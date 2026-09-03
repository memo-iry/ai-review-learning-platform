package com.skala.ailearning.ai;

import java.util.List;

record Concept(
        String name,
        List<String> aliases,
        List<String> reviewPoints,
        String exampleCode,
        AiAnalysisResult.QuizItem quiz
) {
    boolean matches(String text) {
        String lower = text.toLowerCase();
        if (lower.contains(name.toLowerCase())) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> lower.contains(alias.toLowerCase()));
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
                    new AiAnalysisResult.QuizItem(
                            "RAG 파이프라인에서 검색 결과가 비어 있을 때 가장 먼저 확인할 것은?",
                            "질문 임베딩과 문서 임베딩이 같은 모델·같은 차원으로 만들어졌는지 확인한다.")
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
                    new AiAnalysisResult.QuizItem(
                            "RAG 에서 Embedding 의 역할로 가장 적절한 것은?",
                            "문서와 질문을 벡터 공간에 표현해 의미 기반으로 비교할 수 있게 한다.")
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
                    new AiAnalysisResult.QuizItem(
                            "Chunk 크기를 지나치게 작게 잡으면 생기는 문제는?",
                            "문맥이 끊겨 검색은 되더라도 답변에 필요한 정보가 조각 안에 없게 된다.")
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
                    new AiAnalysisResult.QuizItem(
                            "유사도 임계값을 높이면 검색 결과는 어떻게 되는가?",
                            "관련성이 확실한 문서만 남아 결과 수가 줄고, 너무 높이면 아무것도 나오지 않는다.")
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
                    new AiAnalysisResult.QuizItem(
                            "Tool Calling 에서 실제 도구를 실행하는 주체는?",
                            "모델이 아니라 애플리케이션이다. 모델은 호출할 도구와 인자를 결정할 뿐이다.")
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
                    new AiAnalysisResult.QuizItem(
                            "Dockerfile 에서 의존성 설치를 소스 복사보다 앞에 두는 이유는?",
                            "소스만 바뀌었을 때 의존성 레이어 캐시를 재사용해 빌드 시간을 줄이기 위해서다.")
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
                    new AiAnalysisResult.QuizItem(
                            "Deployment 의 replicas 를 3 으로 바꾸면 무슨 일이 일어나는가?",
                            "컨트롤러가 실제 Pod 수를 3 이 되도록 새로 만들거나 줄인다.")
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
                    new AiAnalysisResult.QuizItem(
                            "자식 컴포넌트에서 부모의 상태를 바꾸려면?",
                            "직접 바꾸지 않고 emit 으로 이벤트를 올려 부모가 바꾸게 한다.")
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
                    new AiAnalysisResult.QuizItem(
                            "자원을 새로 만들었을 때 돌려줄 상태 코드는?",
                            "201 Created 이다.")
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
                    new AiAnalysisResult.QuizItem(
                            "N:M 관계를 테이블로 표현하는 방법은?",
                            "양쪽 기본키를 외래키로 갖는 연결 테이블을 만든다.")
            )
    );
}
