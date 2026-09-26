import http from "node:http";
import { URL } from "node:url";
import got from "got";

const PORT = Number(process.env.PORT || 8080);
const RESEARCH_URL = process.env.RESEARCH_URL || "http://127.0.0.1:8081";
const QUANT_ENGINE_API_KEY = process.env.QUANT_ENGINE_API_KEY || "";
function alpacaConfig() {
  if (!process.env.ALPACA_CONFIG) return {};
  try { return JSON.parse(process.env.ALPACA_CONFIG); }
  catch { return {}; }
}

const alpaca = alpacaConfig();
const ALPACA_BASE_URL = (alpaca.base_url || "https://paper-api.alpaca.markets").replace(/\/$/, "");
const ALPACA_API_KEY = alpaca.api_key || "";
const ALPACA_API_SECRET = alpaca.api_secret || "";
const PAPER_HOST = "paper-api.alpaca.markets";
const MAX_BODY_BYTES = 256 * 1024;

function corsHeaders() {
  return {
    "access-control-allow-origin": process.env.CORS_ORIGIN || "*",
    "access-control-allow-methods": "GET,POST,PATCH,DELETE,OPTIONS",
    "access-control-allow-headers": "content-type,x-api-key",
    "access-control-max-age": "600"
  };
}

function writeJson(res, statusCode, body, headers = {}) {
  const payload = JSON.stringify(body);
  res.writeHead(statusCode, {
    "content-type": "application/json; charset=utf-8",
    "content-length": Buffer.byteLength(payload),
    ...corsHeaders(),
    ...headers
  });
  res.end(payload);
}

function writeEmpty(res, statusCode, headers = {}) {
  res.writeHead(statusCode, { ...corsHeaders(), ...headers });
  res.end();
}

function authorized(req) {
  return QUANT_ENGINE_API_KEY && req.headers["x-api-key"] === QUANT_ENGINE_API_KEY;
}

async function readJson(req) {
  const chunks = [];
  let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > MAX_BODY_BYTES) throw Object.assign(new Error("Request body too large"), { statusCode: 413 });
    chunks.push(chunk);
  }
  if (!size) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    throw Object.assign(new Error("Invalid JSON body"), { statusCode: 400 });
  }
}

function alpacaPath(pathname) {
  if (!pathname.startsWith("/v1/trading/")) return null;
  return pathname.slice("/v1/trading".length);
}

function assertPaperEndpoint() {
  const parsed = new URL(ALPACA_BASE_URL);
  if (parsed.protocol !== "https:" || parsed.hostname !== PAPER_HOST) {
    throw new Error("ALPACA_BASE_URL must point to https://paper-api.alpaca.markets");
  }
}

async function alpacaRequest(method, path, searchParams, json) {
  assertPaperEndpoint();
  if (!ALPACA_API_KEY || !ALPACA_API_SECRET) {
    throw Object.assign(new Error("Alpaca paper credentials are not configured"), { statusCode: 503 });
  }

  const response = await got(ALPACA_BASE_URL + path, {
    method,
    searchParams,
    json,
    headers: {
      "APCA-API-KEY-ID": ALPACA_API_KEY,
      "APCA-API-SECRET-KEY": ALPACA_API_SECRET,
      accept: "application/json"
    },
    responseType: "json",
    retry: { limit: 2, methods: ["GET", "HEAD", "OPTIONS"] },
    timeout: { request: 10000 },
    throwHttpErrors: false
  });

  const requestId = response.headers["x-request-id"];
  return {
    statusCode: response.statusCode,
    body: response.body,
    requestId: Array.isArray(requestId) ? requestId[0] : requestId
  };
}

async function proxyResearch(req, res, pathname, search) {
  const url = new URL(RESEARCH_URL + pathname);
  url.search = search;
  const options = {
    method: req.method,
    headers: {
      "x-api-key": req.headers["x-api-key"] || "",
      accept: req.headers.accept || "application/json",
      "content-type": req.headers["content-type"] || "application/json"
    },
    responseType: "json",
    retry: { limit: 1 },
    timeout: { request: 30000 },
    throwHttpErrors: false
  };
  if (req.method === "POST" || req.method === "PATCH") options.json = await readJson(req);
  const response = await got(url, options);
  writeJson(res, response.statusCode, response.body);
}

async function handle(req, res) {
  if (req.method === "OPTIONS") return writeEmpty(res, 204);

  const requestUrl = new URL(req.url, "http://" + (req.headers.host || "localhost"));
  const pathname = requestUrl.pathname;

  if (pathname === "/health" && req.method === "GET") {
    return writeJson(res, 200, {
      status: "ok",
      service: "quant-trader-gateway",
      trading: "alpaca-paper"
    });
  }

  if (!authorized(req)) return writeJson(res, 401, { detail: "Invalid API key" });

  if (pathname.startsWith("/v1/research/")) {
    return proxyResearch(req, res, pathname, requestUrl.search);
  }

  const alpaca = alpacaPath(pathname);
  if (!alpaca) return writeJson(res, 404, { detail: "Not found" });

  const json = new Set(["POST", "PATCH"]).has(req.method) ? await readJson(req) : undefined;
  const result = await alpacaRequest(req.method, alpaca, requestUrl.searchParams, json);
  const headers = result.requestId ? { "x-request-id": result.requestId } : {};

  if (result.statusCode === 204) return writeEmpty(res, 204, headers);
  return writeJson(res, result.statusCode, result.body ?? {}, headers);
}

const server = http.createServer((req, res) => {
  handle(req, res).catch((error) => {
    writeJson(res, Number(error.statusCode) || 502, {
      detail: error.message || "Upstream request failed"
    });
  });
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(JSON.stringify({
    service: "quant-trader-gateway",
    port: PORT,
    alpaca: "paper"
  }));
});
