package com.skala.ailearning.ai;

import java.util.List;

record Concept(
        String name,
        List<String> aliases,
        List<String> reviewPoints,
        String exampleCode,
        Question question
) {
    record Question(
            String text,
            List<String> options,
            int answerIndex,
            String explanation
    ) {
    }

    boolean matches(String text) {
        String lower = text.toLowerCase();
        if (containsToken(lower, name)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> containsToken(lower, alias));
    }

    private static boolean containsToken(String lowerText, String token) {
        String target = token.toLowerCase();
        for (int from = 0; ; ) {
            int start = lowerText.indexOf(target, from);
            if (start < 0) {
                return false;
            }
            int end = start + target.length();
            boolean leftFree = start == 0 || !isAsciiWord(lowerText.charAt(start - 1));
            boolean rightFree = end >= lowerText.length() || !isAsciiWord(lowerText.charAt(end));
            if (leftFree && rightFree) {
                return true;
            }
            from = start + 1;
        }
    }

    private static boolean isAsciiWord(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

    static final List<Concept> ALL = List.of(
            new Concept(
                    "RAG",
                    List.of("검색 증강", "retrieval"),
                    List.of(
                            "RAG 는 검색과 생성을 잇는 구조다 — 질문을 임베딩해 문서를 찾고, 그 문서를 프롬프트에 실어 모델에 넘긴다",
                            "모델이 학습하지 않은 사내 문서를 근거로 답하게 만드는 것이 목적이다",
                            "답변보다 검색 결과를 먼저 확인해야 한다. 근거가 틀렸으면 답도 틀린다"
                    ),
                    """
                    var advisor = RetrievalAugmentationAdvisor.builder()
                            .documentRetriever(VectorStoreDocumentRetriever.builder()
                                    .vectorStore(vectorStore)
                                    .topK(3)
                                    .build())
                            .build();

                    String answer = chatClient.prompt()
                            .advisors(advisor)
                            .user(question)
                            .call()
                            .content();
                    """,
                    new Question(
                            "RAG 파이프라인에서 검색 결과가 비어 있을 때 가장 먼저 확인할 것은?",
                            List.of(
                                    "모델의 temperature 설정",
                                    "질문과 문서가 같은 임베딩 모델로 만들어졌는지",
                                    "시스템 메시지의 길이",
                                    "응답 토큰 상한"
                            ),
                            1,
                            "임베딩 모델이 다르면 벡터 공간이 달라져 유사도 자체가 의미를 잃는다. 차원과 모델을 먼저 맞춘다.")
            ),
            new Concept(
                    "Embedding",
                    List.of("임베딩", "벡터화"),
                    List.of(
                            "임베딩은 텍스트를 고정 길이 실수 벡터로 바꾸는 것이다",
                            "뜻이 가까운 문장은 벡터 공간에서도 가깝다 — 그래서 코사인 유사도로 비교한다",
                            "같은 문장을 두 번 임베딩해도 값이 미세하게 다를 수 있다. 완전히 같은 벡터를 기대하면 안 된다"
                    ),
                    """
                    float[] vector = embeddingModel.embed("환불은 며칠 걸리나요");
                    System.out.println(vector.length);   // 1536
                    """,
                    new Question(
                            "RAG 에서 Embedding 의 역할로 가장 적절한 것은?",
                            List.of(
                                    "AI 응답을 생성한다",
                                    "문서를 벡터 공간에 표현한다",
                                    "프롬프트를 저장한다",
                                    "HTTP 요청을 처리한다"
                            ),
                            1,
                            "임베딩은 텍스트를 숫자 벡터로 바꿔 의미 기반 비교를 가능하게 한다. 생성은 LLM 의 몫이다.")
            ),
            new Concept(
                    "Chunking",
                    List.of("청킹", "분할", "chunk"),
                    List.of(
                            "문서를 통째로 임베딩하면 검색 정확도가 떨어진다. 그래서 조각으로 나눈다",
                            "조각이 너무 크면 관련 없는 내용이 섞이고, 너무 작으면 문맥이 끊긴다",
                            "TokenTextSplitter 는 토큰 수 기준으로 나눈다. 문단 경계를 지키지 않는다는 점을 알고 써야 한다"
                    ),
                    """
                    var splitter = new TokenTextSplitter();
                    List<Document> chunks = splitter.apply(documents);
                    vectorStore.add(chunks);
                    """,
                    new Question(
                            "Chunk 크기를 지나치게 작게 잡으면 생기는 문제는?",
                            List.of(
                                    "임베딩 비용이 급격히 줄어든다",
                                    "문맥이 끊겨 답변에 필요한 정보가 조각 안에 없게 된다",
                                    "벡터 차원이 함께 줄어든다",
                                    "검색 자체가 동작하지 않는다"
                            ),
                            1,
                            "검색은 되지만 조각 안에 맥락이 없어 근거로 쓸 수 없게 된다.")
            ),
            new Concept(
                    "Vector Store",
                    List.of("벡터 스토어", "벡터db", "벡터 db", "pgvector"),
                    List.of(
                            "벡터 스토어는 임베딩과 원문을 함께 저장하고 유사도 검색을 제공한다",
                            "pgvector 는 PostgreSQL 확장이라 기존 DB 를 그대로 쓸 수 있다",
                            "거리 계산 방식(cosine, L2)을 저장할 때와 검색할 때 똑같이 맞춰야 점수가 의미를 가진다"
                    ),
                    """
                    List<Document> found = vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .query("배송 얼마나 걸려요")
                                    .topK(3)
                                    .similarityThreshold(0.5)
                                    .build());
                    """,
                    new Question(
                            "유사도 임계값(similarityThreshold)을 높이면 검색 결과는 어떻게 되는가?",
                            List.of(
                                    "관련성이 확실한 문서만 남아 결과 수가 줄어든다",
                                    "결과 수가 늘어난다",
                                    "임베딩이 다시 계산된다",
                                    "검색 속도가 느려진다"
                            ),
                            0,
                            "너무 높이면 아무것도 나오지 않는다. 0.5 부근에서 시작해 실제 결과를 보며 조정한다.")
            ),
            new Concept(
                    "Tool Calling",
                    List.of("툴 콜링", "함수 호출", "@tool"),
                    List.of(
                            "모델은 어떤 도구를 부를지 결정할 뿐, 실제 실행은 애플리케이션이 한다",
                            "그래서 권한 검사와 승인 게이트는 실행하는 쪽에 둬야 한다",
                            "호출 횟수 상한을 걸지 않으면 모델이 같은 도구를 반복 호출할 수 있다"
                    ),
                    """
                    @Tool(description = "문서를 이름으로 검색한다")
                    public List<String> searchDocuments(
                            @ToolParam(description = "검색어") String keyword) {
                        return repository.findByTitleContaining(keyword);
                    }
                    """,
                    new Question(
                            "Tool Calling 에서 도구를 실제로 실행하는 주체는?",
                            List.of(
                                    "LLM 이 직접 실행한다",
                                    "애플리케이션이 실행한다",
                                    "벡터 스토어가 실행한다",
                                    "프롬프트 템플릿이 실행한다"
                            ),
                            1,
                            "모델은 호출할 도구와 인자를 정할 뿐이다. 그래서 권한 검사를 실행하는 쪽에 둬야 한다.")
            ),
            new Concept(
                    "Docker",
                    List.of("도커", "컨테이너", "이미지"),
                    List.of(
                            "이미지는 읽기 전용 템플릿이고 컨테이너는 그것을 실행한 인스턴스다",
                            "레이어 캐시 때문에 자주 바뀌는 파일은 Dockerfile 뒤쪽에 둬야 빌드가 빠르다",
                            "컨테이너 안의 변경은 컨테이너가 사라지면 함께 사라진다 — 남길 것은 볼륨에 둔다"
                    ),
                    """
                    FROM eclipse-temurin:21-jre
                    WORKDIR /app
                    COPY build/libs/*.jar app.jar
                    ENTRYPOINT ["java", "-jar", "app.jar"]
                    """,
                    new Question(
                            "Dockerfile 에서 의존성 설치를 소스 복사보다 앞에 두는 이유는?",
                            List.of(
                                    "최종 이미지 크기가 줄어든다",
                                    "소스만 바뀌었을 때 의존성 레이어 캐시를 재사용한다",
                                    "컨테이너 실행 속도가 빨라진다",
                                    "포트 충돌을 막는다"
                            ),
                            1,
                            "레이어는 위에서부터 캐시된다. 자주 바뀌는 것을 뒤에 둘수록 빌드가 빨라진다.")
            ),
            new Concept(
                    "Kubernetes",
                    List.of("쿠버네티스", "k8s", "파드", "pod", "deployment"),
                    List.of(
                            "Pod 는 배포의 최소 단위이고, Deployment 가 Pod 개수와 버전을 관리한다",
                            "Service 는 바뀌는 Pod IP 앞에 고정된 주소를 제공한다",
                            "선언한 상태와 실제 상태를 계속 맞추는 것이 컨트롤러의 일이다"
                    ),
                    """
                    apiVersion: apps/v1
                    kind: Deployment
                    spec:
                      replicas: 2
                      template:
                        spec:
                          containers:
                            - name: app
                              image: myapp:1.0
                    """,
                    new Question(
                            "Deployment 의 replicas 를 3 으로 바꾸면 무슨 일이 일어나는가?",
                            List.of(
                                    "기존 Pod 가 모두 삭제된 뒤 새로 만들어진다",
                                    "컨트롤러가 실제 Pod 수를 3 이 되도록 맞춘다",
                                    "Service 의 주소가 함께 바뀐다",
                                    "노드가 3대로 늘어난다"
                            ),
                            1,
                            "선언한 상태와 실제 상태의 차이를 컨트롤러가 계속 메운다. 이것이 선언형 배포다.")
            ),
            new Concept(
                    "Vue",
                    List.of("뷰", "컴포넌트", "vite", "reactive"),
                    List.of(
                            "ref 는 값을 감싸 반응형으로 만들고, .value 로 접근한다",
                            "props 는 부모에서 자식으로 내려가고, 자식은 emit 으로 올린다",
                            "computed 는 의존하는 값이 바뀔 때만 다시 계산된다"
                    ),
                    """
                    const lectures = ref([])

                    onMounted(async () => {
                      lectures.value = await api.getLectures()
                    })
                    """,
                    new Question(
                            "자식 컴포넌트에서 부모의 상태를 바꾸려면 어떻게 해야 하는가?",
                            List.of(
                                    "props 를 직접 수정한다",
                                    "emit 으로 이벤트를 올려 부모가 바꾸게 한다",
                                    "computed 에 값을 대입한다",
                                    "ref 를 전역에 등록한다"
                            ),
                            1,
                            "데이터는 아래로, 이벤트는 위로. props 를 직접 고치면 흐름이 끊긴다.")
            ),
            new Concept(
                    "Vue Router",
                    List.of("라우터", "라우팅", "routerview", "router-view", "router.push", "중첩 라우"),
                    List.of(
                            "부모 라우트의 children 배열에 자식 라우트를 등록한다",
                            "자식 라우트의 path 앞에는 슬래시를 붙이지 않는다. 붙이면 절대 경로가 된다",
                            "자식 컴포넌트는 부모 컴포넌트의 RouterView 자리에 그려진다",
                            "페이지가 바뀌어도 남아야 하는 값은 컴포넌트가 아니라 store 에 둔다",
                            "router.push 에 경로 대신 이름을 쓰면 주소가 바뀌어도 코드를 안 고쳐도 된다"
                    ),
                    """
                    const routes = [
                      {
                        path: '/home',
                        name: 'home',
                        component: () => import('../pages/HomePage.vue'),
                        children: [
                          { path: 'analysis', name: 'analysis',
                            component: () => import('../pages/AnalysisPage.vue') },
                        ],
                      },
                    ]

                    router.push({ name: 'analysis' })   // /home/analysis
                    """,
                    new Question(
                            "children 에 적는 path 앞에 슬래시를 붙이면 어떻게 되는가?",
                            List.of(
                                    "부모 경로 아래 상대 경로로 연결된다",
                                    "부모 경로를 무시한 절대 경로가 된다",
                                    "라우트 이름이 자동으로 생성된다",
                                    "아무 차이가 없다"
                            ),
                            1,
                            "'/analysis' 로 적으면 /home 아래가 아니라 최상위 경로가 된다.")
            ),
            new Concept(
                    "REST API",
                    List.of("rest", "엔드포인트", "http 메서드"),
                    List.of(
                            "자원은 명사로, 행위는 HTTP 메서드로 표현한다",
                            "생성은 201, 조회는 200, 없는 자원은 404 를 돌려준다",
                            "요청 본문과 응답 본문의 형식을 먼저 정하면 프론트와 백엔드가 동시에 작업할 수 있다"
                    ),
                    """
                    @PostMapping("/api/reflections")
                    @ResponseStatus(HttpStatus.CREATED)
                    public ReflectionResponse create(@Valid @RequestBody ReflectionRequest request) {
                        return service.create(request);
                    }
                    """,
                    new Question(
                            "자원을 새로 만들었을 때 돌려줄 상태 코드는?",
                            List.of("200 OK", "201 Created", "204 No Content", "302 Found"),
                            1,
                            "생성은 201 이다. 200 은 조회, 204 는 본문 없는 성공, 302 는 리다이렉트다.")
            ),
            new Concept(
                    "ERD",
                    List.of("정규화", "데이터 모델링", "외래키", "n:m"),
                    List.of(
                            "1:N 은 자식 쪽에 외래키를 둔다",
                            "N:M 은 연결 테이블로 푼다. 관계 자체에 속성이 있으면 그 테이블에 함께 둔다",
                            "PostgreSQL 은 MySQL 과 달리 외래키에 인덱스를 자동으로 만들지 않는다"
                    ),
                    """
                    CREATE TABLE reflections (
                        user_id    BIGINT NOT NULL REFERENCES users(user_id),
                        lecture_id BIGINT NOT NULL REFERENCES lectures(lecture_id),
                        CONSTRAINT uk_user_lecture UNIQUE (user_id, lecture_id)
                    );
                    """,
                    new Question(
                            "N:M 관계를 테이블로 표현하는 방법은?",
                            List.of(
                                    "한쪽 테이블에만 외래키를 둔다",
                                    "양쪽 기본키를 외래키로 갖는 연결 테이블을 만든다",
                                    "두 테이블을 하나로 합친다",
                                    "외래키 없이 인덱스만 만든다"
                            ),
                            1,
                            "연결 테이블에 관계 고유의 속성까지 함께 둘 수 있다. reflections 가 그 예다.")
            )
    );
}
