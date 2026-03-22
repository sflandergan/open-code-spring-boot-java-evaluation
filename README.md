# LLM Evaluation — Hobby Coding on Mac M4 Mini

This repository evaluates the practical usefulness of local and cloud LLMs for hobby software development on a **Mac M4 Mini with 32 GB RAM**. 
The goal is to find a suitable setup for everyday feature development given real-world constraints: cost, speed, and code quality.

## Table of Contents

- [Goal](#goal)
- [Setup](#setup)
- [Evaluation Task](#evaluation-task)
- [Models Evaluated](#models-evaluated)
- [Results: Planning Phase](#results-planning-phase)
- [Results: Implementation Phase](#results-implementation-phase)
- [Models That Failed Early](#models-that-failed-early)
- [Recommendations](#recommendations)
- [Repository Structure](#repository-structure)

---

## Goal

To answer the question: **Which LLMs are well suited for a hobby programmer using a Mac M4 Mini?**

The evaluation focuses on:

- **Local models** — run entirely on-device via LM Studio (no API costs, private, but limited by hardware)
- **Cloud models** — accessed via GitHub Copilot and [Requesty](https://requesty.ai) (API costs apply, more capable)

The task is a realistic feature request in a non-trivial Spring Boot codebase with strict architectural rules defined in `AGENTS.md`.

---

## Setup

**Hardware**: Apple Mac M4 Mini, 32 GB unified memory

**Tool**: [OpenCode](https://opencode.ai) — AI coding agent for the terminal

**Local model runtime**: LM Studio

**Cloud access**: GitHub Copilot (Claude models) and Requesty (various providers)

---

## Evaluation Task

Each model was given a two-shot task:

### Shot 1 — Planning

Each model was asked to produce a reviewable implementation plan to be used as a baseline for implementing the feature step by step.
The following prompt was used:

```
We need a new feature for managing devices. It should have a REST API to:
- Create devices
- Assign sensors to devices (idempotent PUT operation)
- Update devices
- Delete devices

A device has the following properties: Name, Description

Create an implementation plan and store it as markdown named `devices-feature.md`.
```

Note, that the plan is quite short and leaves some open questions.
In a real world example, you would probably add more details and a feedback loop question like:

```
Before creating the plan ask me questions that will improve the result.
```

### Shot 2 — Implementation

This step translates the plan into code.
The prompt can be simple since all details are in the plan:

```
Implement the plan defined in `@devices-feature.md`.
```

**Note**: All models implemented based on the same baseline plan (Claude Haiku 4.5's output) to ensure a fair comparison of coding ability independent of planning quality.

---

## Models Evaluated

### Local (free, on-device)

Only models fitting within the 32 GB memory limit and optimized for MLX were tested.
Some models were removed from detailed evaluation due to significant integration issues with OpenCode.

| Model | LM Studio ID |
|-------|-------------|
| Devstral Small 2 25.12 | [mistralai/devstral-small-2-2512](https://lmstudio.ai/models/mistralai/devstral-small-2-2512) |
| GPT-OSS Safeguard 20B MLX MXFP4 | [gpt-oss-safeguard-20b-MLX-MXFP4](https://lmstudio.ai/models/openai/gpt-oss-safeguard-20b) |
| Nemotron 3 Nano 30B A3B MLX 4bit | [NVIDIA-Nemotron-3-Nano-30B-A3B-MLX-4bit](https://lmstudio.ai/models/nvidia/nemotron-3-nano) |
| Gemma 3 12B 4bit | [google/gemma-3-12b](https://lmstudio.ai/models/google/gemma-3-12b) |

### Cloud via GitHub Copilot

[Github Copilot's Pro subscription](https://github.com/features/copilot/plans) gives access to various LLMs well suited for development.
The pricing is prompt-based, with each model consuming a different number of prompt credits.
The evaluation was limited to Anthropic's models based on previous experiences.

| Model | Costs |
|-------|----------------|
| Claude Haiku 4.5 | 1 credit worth $0.03 - $0.04 |
| Claude Sonnet 4.6 | 1 credit worth $0.03 - $0.04 |
| Claude Opus 4.6 | 3 credits worth $0.03 - $0.04 |

### Cloud via Requesty

[Requesty](https://www.requesty.ai/) is a gateway to different LLM providers similar to [OpenRouter](https://openrouter.ai/).
Requesty was picked to fulfill the side goal of finding an European alternative to OpenRouter.
The pricing is based on input and output tokens, with some models supporting caching to reduce costs.
Expensive, proprietary models were not evaluated.

| Model | Provider | Costs (Input, Output, Cache write, Cache read) |
|-------|----------|----------------|
| MiniMax M2.5 | MiniMaxAI | $0.30/M $1.20/M $1.20/M $0.060/M |
| Kimi K2.5 | Moonshot AI (via Nebius) | $0.500/M $2.50/M - - |
| Devstral | Mistral AI | $0.400/M $2.00/M - - |
| GLM 4.7 | Zhipu AI (via Nebius) | $0.400/M $2.00/M - - |
| Qwen3 Coder 480B | Alibaba | $0.400/M $1.60/M - - |
| Qwen Turbo | Alibaba | $0.050/M $0.200/M - - |

**Notes**: Devstral was free when starting the evaluation. 
Requesty changed this during the evaluation.
GLM 4.7 has been tested in favor of GLM 5 since it was available on a European provider.

---

## Results: Planning Phase

The planning prompt asked each model to produce a structured implementation plan.

| Model | Type | Time | Cost | Plan Score |
|-------|------|------|------|-----------|
| **Claude Opus 4.6** | GitHub | 3m 8s | $0.09 - $0.12 (subscription) | **36/40** |
| **Claude Sonnet 4.6** | GitHub | 2m 23s | $0.03 - $0.04 (subscription) | **35/40** |
| **GLM 4.7** | Requesty | 2m 57s | ~$0.25 | **34/40** |
| **Kimi K2.5** | Requesty | 35s | ~$0.10 | **29/40** |
| **MiniMax M2.5** | Requesty | 1m 37s | ~$0.02 | **27/40** |
| **Devstral** | Requesty | 17s | ~$0.01 | **26/40** |
| **Claude Haiku 4.5** *(baseline)* | GitHub | 35s | $0.03 - $0.04 (subscription) | **25/40** |
| **Nemotron 3 Nano 30B** | Local | 1m 42s | free | **14/40** |
| **DeepSeek-V3.2** | Requesty | 42s | $0.05 | **12/40** |
| **Devstral Small 2** | Local | 1m 28s | free | **12/40** |
| **GPT-OSS Safeguard 20B** | Local | 2m 41s | free | **9/40** |
| **Qwen Turbo** | Requesty | 58s | <$0.01 | **8/40** |

> See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Results: Implementation Phase

All models implemented the feature using the Claude Haiku 4.5 plan as a shared baseline.

| Model | Type | Time | Cost | Impl Score | Notes |
|-------|------|------|------|-----------|-------|
| **Claude Sonnet 4.6** | GitHub | 10m | $0.03 - $0.04 (subscription) | **34/40** | Fully implemented, self-corrected Lombok usage, correct cross-feature layering |
| **GLM 4.7** | Requesty | 12m | ~$0.52 | **33/40** | Fully implemented, richest sensor response via JPA graph traversal |
| **Claude Haiku 4.5** | GitHub | 13m | $0.03 - $0.04 (subscription) | **30/40** | Needed a second prompt to fix startup issues; sensor list always empty in response |
| **Claude Opus 4.6** | GitHub | 12m | $0.09 - $0.12 (subscription) | **28/40** | Fully implemented, bypassed layering rule via DB exception catch |
| **Kimi K2.5** | Requesty | 6m | ~$1.74 | **27/40** | Fully implemented, fastest cloud; critical `findAll().stream().filter()` performance bug |
| **MiniMax M2.5** | Requesty | 12m | ~$0.24 | **26/40** | Fully implemented; sensor names/types missing from response despite being mapped |
| **Devstral** | Requesty | 28m | free* | **24/40** | Fully implemented but sensor retrieval left as hardcoded empty list placeholder |
| **Devstral Small 2** | Local | 54m | free | **13/40** | Only data layer implemented; no service, controller, or DTOs |
| **DeepSeek-V3.2** | Requesty | aborted (6m) | ~$0.52 | **8/40** | Aborted; only partial data layer, no service or controller |
| **Nemotron 3 Nano 30B** | Local | 54m | free | **11/40** | Very slow, stopped without feedback; wrong sensor ID type, no tests |
| **GPT-OSS Safeguard 20B** | Local | 18m | free | **5/40** | Used `@Service`/`@Component`, needed constant prompting |
| **Qwen Turbo** | Requesty | 12m | ~$0.16 | **4/40** | Repeated tool call failures; code written to wrong directories, broke the build |

**Notes**: Devstral was free when starting the evaluation. 
Requesty changed this during the evaluation.

> See [`devices-implementation-evaluation.md`](devices-implementation-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Models That Failed Early

Several models were tested but failed so severely that full evaluation was not meaningful:

| Model | Type | Failure Reason |
|-------|------|----------------|
| [Qwen3 Coder 30B](https://lmstudio.ai/models/qwen/qwen3-coder-30b) | Local | Constant context compactions and crashes |
| [Qwen3.5 27B 4bit](https://huggingface.co/mlx-community/Qwen3.5-27B-4bit) | Local | Stuck reading AGENTS.md (did not finish after 40 minutes) |
| [Qwen3.5 9B](https://lmstudio.ai/models/qwen/qwen3.5-9b) | Local | Stuck reading AGENTS.md |
| [LFM2 24B A2B](https://lmstudio.ai/models/liquid/lfm2-24b-a2b) | Local | Failed to call tools properly |
| [GLM-4.6V-Flash 8Bit](https://lmstudio.ai/models/glm-4.6v-flash) | Local | Stuck in thinking |
| Gemma 3 12B 4bit | Local | Constant tool call failures |
| Gemini 3.1 Flash Lite | Requesty | Failed to call tools consistently |

---

## Recommendations

Rather than looking for a single best model, the more useful question is: **which model is best for each role in your workflow?** Planning and implementation are different tasks — a model that writes excellent plans does not necessarily write the best code, and vice versa.

### Planning: Claude Opus 4.6 or Claude Sonnet 4.6 (GitHub Copilot)

Opus produces the best plans (36/40), with the strongest architectural reasoning, explicit cross-cutting concerns, and the most accurate codebase alignment. Sonnet follows closely (35/40) with the cleanest API design and most complete configuration examples. Both are on a flat GitHub Copilot subscription, so there is no per-use cost to choose the stronger planner.

For pay-per-use, **GLM 4.7** is the best cloud planner at ~$0.25 per plan (34/40), with detailed Java code examples and a rich test plan.

### Implementation: Claude Sonnet 4.6 (GitHub Copilot)

Sonnet 4.6 produces the best implementation (34/40), self-corrects style issues, correctly uses `SensorService` for cross-feature validation, and completes in ~10 minutes. On a flat subscription this has no marginal cost.

For pay-per-use implementation, **GLM 4.7** (~$0.52, 33/40) is the strongest alternative and the only other model to return full sensor details in the API response.

### Recommended Setup

**Subscription (GitHub Copilot):**
Use **Opus 4.6** or **Sonnet 4.6** to produce the plan, review and adjust it, then use **Sonnet 4.6** to implement. Both models on the same flat subscription — no extra cost for the two-step workflow.

**Pay-per-use (Requesty):**
Use **GLM 4.7** for planning (~$0.25) and implementation (~$0.52). Total cost per feature: ~$0.77. The quality gap vs. subscription models is modest.

**Best value hybrid:**
Use **GLM 4.7** for planning (~$0.25 via Requesty) and **MiniMax M2.5** for implementation (~$0.24). Total ~$0.49 per feature — the lowest cost for a fully functional result.

### Local Models

No local model came close to matching cloud models. Devstral Small 2 scored highest locally (13/40) but only produced the data layer — no service, controller, or DTOs. Nemotron 3 Nano 30B (11/40) took 54 minutes and stopped without feedback. Gemma 3 12B failed entirely due to constant tool call failures. **Local models on a Mac M4 Mini 32 GB are not viable for this kind of structured, multi-file Spring Boot feature development** — they lack the context handling, tool-calling reliability, and instruction-following needed for either planning or implementation.

### Models to Avoid

- **Devstral (Requesty)**: Most expensive cloud option (~$2.46 for implementation) with mediocre results (24/40).
- **Qwen Turbo**: Cheapest cloud option but consistently failed at tool calling — not usable for either role.
- **All local models**: Unreliable tool calling, wrong Java style, very slow.

### Summary Table

| Role | Model | Cost | Score | Notes |
|------|-------|------|-------|-------|
| **Planning** | Claude Opus 4.6 | subscription | 36/40 | Best plan quality |
| **Planning** | Claude Sonnet 4.6 | subscription | 35/40 | Best API design |
| **Planning** | GLM 4.7 | ~$0.25 | 34/40 | Best pay-per-use planner |
| **Implementation** | Claude Sonnet 4.6 | subscription | 34/40 | Best implementation quality |
| **Implementation** | GLM 4.7 | ~$0.52 | 33/40 | Best pay-per-use implementer |
| **Implementation** | Claude Haiku 4.5 | subscription | 30/40 | Good, lower subscription tier |
| **Implementation** | Claude Opus 4.6 | subscription | 28/40 | Weaker than Sonnet or Haiku on implementation |
| **Implementation** | MiniMax M2.5 | ~$0.24 | 26/40 | Lowest cost for a fully functional result |

---

## Repository Structure

```
.
├── README.md                        # This file
├── AGENTS.md                        # AI assistant instructions for this codebase
├── devices-feature.md               # Baseline implementation plan (Claude Haiku 4.5)
├── devices-plan-evaluation.md       # Detailed planning phase evaluation
├── devices-implementation-evaluation.md  # Detailed implementation phase evaluation
├── opencode.json                    # OpenCode configuration (models, providers)
├── pom.xml                          # Maven project descriptor
├── config/                          # Application configuration
├── docs/patterns/                   # Architectural pattern documentation
└── src/                             # Spring Boot application source
    ├── main/java/de/sfl/
    │   ├── sensors/                 # Example feature (Sensor management)
    │   └── ...
    └── test/
```

### Branch Structure

Each model's output lives in its own branch:

| Branch pattern | Description |
|----------------|-------------|
| `gh-<model>/device-feature` | GitHub Copilot model implementation |
| `rq-<model>/devices-feature` | Requesty model implementation |
| `<model>/devices-feature` | Local model implementation |
| `evaluation-summary` | Merged evaluation documents |

### Technologies

- **Java 21** with records, sealed classes, pattern matching
- **Spring Boot 3.x** with constructor injection and `@Configuration` beans
- **PostgreSQL 17** with Flyway migrations
- **Spring Data JPA** with keyset pagination
- **TestContainers** for repository integration tests
- **MockMvc** for controller tests
- **Springdoc OpenAPI** for API documentation
