# RAG · AI System Prompt 설계

이번 단계에서는 실제 LLM 을 붙이지 않습니다. 대신 **AI 가 들어올 자리를 계약으로 고정해 두고**
규칙 기반 구현으로 전체 흐름을 검증했습니다. 이 문서는 그 자리에 무엇이 들어갈지를 적습니다.

## 지금 구조

```
Controller → Service → AiAnalysisPort ─┬─ MockAiAnalysisAdapter   @Profile("!openai")  현재
                                       └─ OpenAiAnalysisAdapter   @Profile("openai")   이후
```

```java
public interface AiAnalysisPort {
    AiAnalysisResult analyze(AnalysisCommand command);
}
```

교체 지점은 이 인터페이스 한 곳입니다. 컨트롤러 · 서비스 · DB · 화면은 고치지 않습니다.

### 입력

```java
record AnalysisCommand(
    String       lectureTitle,        // 강의명
    String       lectureDescription,  // 강의 설명
    List<String> materialTitles,      // 강의자료 제목
    String       understood,          // 회고 · 이해한 것
    String       difficult,           // 회고 · 어려웠던 것
    String       wantsToLearn         // 회고 · 더 알고 싶은 것
)
```

RAG 를 붙일 때 이 구조를 바꾸지 않습니다. **검색된 강의자료 본문을 필드 하나로 더하면 됩니다.**

```java
    List<RetrievedChunk> context      // 추가될 필드
```

### 출력

```java
record AiAnalysisResult(
    // 분석
    int                 understandingScore,   // 0-100
    String              analysisReason,
    String              understoodSummary,
    String              weaknessSummary,
    List<String>        understoodTopics,
    List<String>        weakTopics,
    List<String>        recommendedTopics,
    // 복습자료
    String              reviewTitle,
    List<String>        coreConcepts,
    String              exampleCode,
    // 확인 문제
    List<QuizItem>      quiz,                 // conceptName · question · options · answerIndex · explanation
    // 개념별 상태 서술 — concept_mastery.summary 에 저장되어 다음 프롬프트에 실린다
    Map<String, String> conceptSummaries
)
```

## RAG 파이프라인

```
강의 PDF
   ↓  Parsing            본문 추출
   ↓  Chunking           토큰 기준 분할
   ↓  Embedding          벡터화
   ↓  Vector Store       pgvector — 이미 Supabase PostgreSQL 을 쓰므로 확장으로 붙인다
   ↓
회고 → Query 생성         difficult 와 wantsToLearn 을 검색어로
   ↓  유사도 검색          해당 강의의 자료로 범위를 제한한다
   ↓
System Prompt + Retrieved Context + 학습자 회고 + 누적 이해도
   ↓  LLM
AiAnalysisResult
```

검색 범위를 **해당 강의의 자료로 제한**하는 것이 중요합니다. 전체 강의에서 검색하면
아직 배우지 않은 내용이 복습자료에 섞입니다.

## 프롬프트를 셋으로 나눕니다

한 번의 호출로 분석 · 복습자료 · Quiz 를 모두 만들면 각각의 품질 기준을 따로 걸 수 없습니다.
역할을 나누되 **포트는 그대로 둡니다** — 어댑터 안에서 세 번 호출하고 하나의 결과로 조립합니다.

```
OpenAiAnalysisAdapter.analyze(command)
    ├─ 1. Reflection Analyzer   → understandingScore · topics · summaries
    ├─ 2. Review Generator      → reviewTitle · coreConcepts · exampleCode · conceptSummaries
    └─ 3. Quiz Generator        → quiz
         └─ 셋을 합쳐 AiAnalysisResult 하나로 반환
```

포트를 셋으로 쪼개면 서비스와 컨트롤러가 순서를 알아야 하고, 교체 지점이 하나라는 성질을 잃습니다.

| 프롬프트 | 입력 | 출력 |
|---|---|---|
| Reflection Analyzer | 회고 + 강의 정보 + 누적 이해도 | 이해 / 취약 개념, 점수, 근거 |
| Review Generator | 취약 개념 + 검색된 강의자료 | 복습 설명, 예제 코드, 개념별 서술 |
| Quiz Generator | 취약 개념 + 검색된 강의자료 | 4지선다 문항, 정답, 해설 |

## 대표 시스템 프롬프트 — Reflection Analyzer

세 개 중 이것을 대표로 싣습니다. 나머지 둘의 입력이 여기서 나오고,
지어내지 않는다는 규칙이 가장 중요하게 걸리는 자리이기 때문입니다.

````text
당신은 교육생의 학습 회고를 분석하는 조교입니다.
회고에 실제로 적힌 내용만 근거로 판단합니다.

## 규칙

1. 회고에 없는 사실을 만들어내지 않습니다.
   "아마 이것도 모를 것이다" 같은 추측을 하지 않습니다.
   판단의 근거는 반드시 회고 문장 안에 있어야 합니다.

2. 강의자료(Context)에 없는 개념은 취약 개념으로 잡지 않습니다.
   이번 강의에서 다루지 않은 내용은 복습 대상이 아닙니다.

3. 같은 개념이 이해한 것과 어려운 것에 모두 나오면 취약으로 분류합니다.
   학습자가 어렵다고 말한 쪽을 우선합니다.

4. 개념 이름은 Context 에 등장한 표기를 그대로 씁니다.
   임의로 번역하거나 축약하지 않습니다.

5. 회고가 너무 짧거나 판단할 근거가 없으면 weakTopics 를 비우고
   analysisReason 에 그 사실을 적습니다. 억지로 채우지 않습니다.

## 점수 기준

understandingScore 는 이번 회고에서 드러난 이해 수준입니다. 0-100.
이해한 개념이 많을수록 올리고, 어려워한 개념이 많을수록 내립니다.
누적 이해도가 주어지면 그 흐름을 함께 고려하되, 이번 회고의 내용을 우선합니다.

## 입력

강의명: {{lectureTitle}}
강의 설명: {{lectureDescription}}
강의자료: {{materialTitles}}

검색된 강의자료 내용:
{{retrievedContext}}

이 학습자의 누적 이해도:
{{conceptMasterySummaries}}

회고 — 이해한 내용:
{{understood}}

회고 — 어려웠던 내용:
{{difficult}}

회고 — 더 알고 싶은 내용:
{{wantsToLearn}}

## 출력

아래 JSON 만 출력합니다. 설명이나 코드펜스를 덧붙이지 않습니다.

```json
{
  "understandingScore": 0,
  "analysisReason": "이번 회고에서 무엇을 근거로 이렇게 판단했는지 두 문장 이내",
  "understoodSummary": "이해한 부분을 학습자에게 하는 말투로 한 문단",
  "weaknessSummary": "보완이 필요한 부분을 한 문단",
  "understoodTopics": ["개념명"],
  "weakTopics": ["개념명"],
  "recommendedTopics": ["개념명"],
  "conceptSummaries": {
    "개념명": "이 학습자가 이 개념에서 지금 어떤 상태인지. 회고의 어느 문장을 근거로 그렇게 보았는지 포함한다."
  }
}
```
````

### 출력이 코드와 1:1 로 대응합니다

| JSON 키 | Java 필드 | 저장 위치 |
|---|---|---|
| `understandingScore` | `AiAnalysisResult.understandingScore` | `ai_analyses.understanding_score` |
| `analysisReason` | `analysisReason` | `ai_analyses.summary` |
| `understoodSummary` · `weaknessSummary` | 동일 | `ai_analyses.understood_summary` · `weakness_summary` |
| `understoodTopics` · `weakTopics` · `recommendedTopics` | 동일 | `ai_analyses` 의 JSONB 컬럼 |
| `conceptSummaries` | `conceptSummaries` | `concept_mastery.summary` |

파싱 실패에 대비해 어댑터는 재시도 한 번 후 규칙 기반 결과로 물러납니다.
분석이 실패했다고 화면이 비면 안 됩니다.

## 현재 구현에서 그대로 가져갈 원칙

`MockAiAnalysisAdapter` 는 LLM 이 아니라 규칙 기반입니다. 회고에서 개념명을 찾아
이해 / 취약으로 나누고, 점수를 계산해 복습 지점과 Quiz 를 만듭니다.

그 과정에서 세운 규칙을 위 프롬프트에 그대로 옮겼습니다.

```java
// 분석해서 알아낸 것(이해 / 취약)과 그 판단의 근거가 된 회고 원문만 담는다.
// Mock 이 없는 사실을 지어내지 않도록 한다.
private static Map<String, String> conceptSummaries(...)
```

```java
// 같은 개념이 이해와 취약에 모두 나오면 취약을 우선한다.
understood.removeAll(weak);
```

```java
// 개념 이름이 다른 단어 안에 들어 있는 경우를 걸러낸다. "Vue" 가 "RouterView" 안에서 걸리지 않도록.
private static boolean containsToken(String lowerText, String token)
```

규칙 기반으로 만들면서 **무엇이 틀릴 수 있는지**를 먼저 겪었고, 그 목록이 프롬프트의 제약 조건이 됐습니다.

## MVP 범위 밖

이번 단계에서는 구현하지 않습니다.

```
실제 LLM 호출 · PDF Parsing · Chunking · Embedding · Vector DB · RAG 질의응답
```

대신 이 문서의 계약과 프롬프트가 그 자리를 정의합니다.
