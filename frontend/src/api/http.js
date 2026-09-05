import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export class ApiError extends Error {
  constructor(message, { status = 0, code, data } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.data = data;
  }

  get isNetworkError() {
    return this.status === 0;
  }

  get isUnauthorized() {
    return this.status === 401;
  }

  get isForbidden() {
    return this.status === 403;
  }
}

export const http = axios.create({
  baseURL: API_BASE_URL,

  withCredentials: true,
});

let sessionExpiredHandler = null;

export function setSessionExpiredHandler(handler) {
  sessionExpiredHandler = handler;
}

http.interceptors.response.use(
  (response) => {
    if (response.status === 204) return null;
    return response.data;
  },
  (error) => {
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    if (!error.response) {
      return Promise.reject(
        new ApiError("서버에 연결할 수 없습니다.", { status: 0 }),
      );
    }

    const { status, data, config } = error.response;

    if (status === 401 && !config?.skipAuthHandler) {
      sessionExpiredHandler?.();
    }

    return Promise.reject(
      new ApiError(data?.message ?? "요청에 실패했습니다.", {
        status,
        code: data?.code,
        data,
      }),
    );
  },
);
