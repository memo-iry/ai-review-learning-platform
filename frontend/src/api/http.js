import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

/**
 * 서버가 내려준 상태 코드를 보존하는 에러.
 */
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
  // 세션 쿠키를 항상 실어 보낸다. 인증이 필요 없는 엔드포인트는 서버가 무시하면 그만이라
  // "인증용 인스턴스"를 따로 둘 이유가 없다.
  withCredentials: true,
});
// Content-Type 은 전역 기본값으로 두지 않는다.
// axios 는 body 가 객체일 때만 application/json 을 자동으로 붙이므로,
// GET 요청에 불필요한 헤더가 실려 preflight 를 유발하는 일이 없다.

/**
 * 세션 만료 시 실행할 동작을 앱 부트스트랩에서 주입한다.
 * (예: main.js 에서 auth store 초기화 + router.push("/login"))
 * 여기서 router 를 직접 import 하면 api 레이어가 라우터에 묶이므로 주입 방식을 쓴다.
 */
let sessionExpiredHandler = null;

export function setSessionExpiredHandler(handler) {
  sessionExpiredHandler = handler;
}

http.interceptors.response.use(
  (response) => {
    // 기존 fetch 인터페이스와 동일하게 파싱된 body 를 바로 돌려준다.
    // 204 는 axios 가 data 를 빈 문자열로 주므로 null 로 정규화한다.
    if (response.status === 204) return null;
    return response.data;
  },
  (error) => {
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    // 네트워크 단절, CORS 차단, 타임아웃 등 응답 자체가 없는 경우
    if (!error.response) {
      return Promise.reject(
        new ApiError("서버에 연결할 수 없습니다.", { status: 0 }),
      );
    }

    const { status, data, config } = error.response;

    // /auth/me 처럼 401 이 정상 응답인 호출은 skipAuthHandler 로 전역 처리를 건너뛴다.
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
