# LLM Evaluation — Hobby Coding on Mac M4 Mini

This repository evaluates the practical usefulness of local and cloud LLMs for hobby software development on a **Mac M4 Mini with 32 GB RAM**. The goal is to find the best model for everyday feature development given real-world constraints: cost, speed, and code quality.

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

To answer the question: **Which LLM is best suited for a hobby programmer using a Mac M4 Mini?**

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

> We need a new feature for managing devices. It should have a REST API to:
> - Create devices
> - Assign sensors to devices (idempotent PUT operation)
> - Update devices
> - Delete devices
>
> A device has the following properties: Name, Description
>
> Create an implementation plan and store it as markdown named `devices-feature.md`.

### Shot 2 — Implementation

> Implement the plan defined in `@devices-feature.md`.

**Note**: All models implemented based on the same baseline plan (Claude Haiku 4.5's output) to ensure a fair comparison of coding ability independent of planning quality.

### Scoring

The implementation was scored out of **40 points** across test coverage, code quality, AGENTS.md compliance, and correctness. See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) for the detailed planning rubric and scores.

---

## Models Evaluated

### Local (free, on-device)

| Model | LM Studio ID |
|-------|-------------|
| Devstral Small 2 25.12 | `mistralai/devstral-small-2-2512` |
| GPT-OSS Safeguard 20B MLX MXFP4 | `gpt-oss-safeguard-20b-MLX-MXFP4` |
| Nemotron 3 Nano 30B A3B MLX 4bit | `NVIDIA-Nemotron-3-Nano-30B-A3B-MLX-4bit` |
| Gemma 3 27B 4bit | `google/gemma-3-27b` (4bit) |
| Gemma 3 12B 4bit | `google/gemma-3-12b` (4bit) |

### Cloud via GitHub Copilot

| Model | Cost structure |
|-------|----------------|
| Claude Haiku 4.5 | Flat subscription |
| Claude Sonnet 4.6 | Flat subscription |
| Claude Opus 4.6 | Flat subscription |

### Cloud via Requesty

| Model | Provider |
|-------|----------|
| MiniMax M2.5 | MiniMaxAI |
| Kimi K2.5 | Moonshot AI (via Nebius) |
| Devstral | Mistral AI |
| GLM 4.7 | Zhipu AI (via Nebius) |
| Qwen3 Coder 480B | Alibaba |
| Qwen Turbo | Alibaba |

---

## Results: Planning Phase

The planning prompt asked each model to produce a structured implementation plan.

| Model | Type | Time | Cost | Plan Score |
|-------|------|------|------|-----------|
| **Claude Opus 4.6** | GitHub | 3m 8s | subscription | **36/40** |
| **Claude Sonnet 4.6** | GitHub | 2m 23s | subscription | **35/40** |
| **GLM 4.7** | Requesty | 2m 57s | ~$0.25 | **34/40** |
| **Kimi K2.5** | Requesty | 35s | ~$0.10 | **29/40** |
| **MiniMax M2.5** | Requesty | 1m 37s | ~$0.02 | **27/40** |
| **Devstral** | Requesty | 17s | ~$0.01 | **26/40** |
| **Claude Haiku 4.5** *(baseline)* | GitHub | 35s | subscription | **25/40** |
| **Nemotron 3 Nano 30B** | Local | 1m 42s | free | **14/40** |
| **Devstral Small 2** | Local | 1m 28s | free | **12/40** |
| **GPT-OSS Safeguard 20B** | Local | 2m 41s | free | **9/40** |
| **Qwen Turbo** | Requesty | 58s | <$0.01 | **8/40** |

> See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Results: Implementation Phase

All models implemented the feature using the Claude Haiku 4.5 plan as a shared baseline.

| Model | Type | Time | Cost | Impl Score | Notes |
|-------|------|------|------|-----------|-------|
| **Claude Sonnet 4.6** | GitHub | 10m | subscription | **33/40** | Fully implemented, self-corrected Lombok usage |
| **Claude Haiku 4.5** | GitHub | 13m | subscription | **31/40** | Needed a second prompt to fix startup issues |
| **GLM 4.7** | Requesty | 12m | ~$0.52 | **29/40** | Fully implemented |
| **Kimi K2.5** | Requesty | 6m | ~$1.74 | **25/40** | Fully implemented, fastest cloud |
| **MiniMax M2.5** | Requesty | 12m | ~$0.24 | **25/40** | Fully implemented |
| **Devstral** | Requesty | 28m | ~$2.46 | **24/40** | Fully implemented but slow and expensive |
| **Claude Opus 4.6** | GitHub | 12m | subscription | **26/40** | Fully implemented |
| **Nemotron 3 Nano 30B** | Local | 54m | free | **13/40** | Very slow, stopped without feedback |
| **GPT-OSS Safeguard 20B** | Local | 18m | free | **5/40** | Did not use Java 21 features, needed constant prompting |
| **Gemma 3 27B 4bit** | Local | 30m | free | **5/40** | Could not write to src folder, used Lombok |
| **Gemma 3 12B 4bit** | Local | ~30m | free | **5/40** | Only partially implemented classes |
| **Devstral Small 2** | Local | stopped | free | **0/40** | Stopped after 54m without any output |
| **Qwen3 Coder 480B** | Requesty | aborted (6m) | ~$0.52 | **8/40** | Aborted due to continuous wrong folder access |
| **Qwen Turbo** | Requesty | 12m | ~$0.16 | **4/40** | Repeated tool call failures |

---

## Models That Failed Early

Several models were tested but failed so severely that full evaluation was not meaningful:

| Model | Type | Failure Reason |
|-------|------|----------------|
| Qwen3 Coder 30B | Local | Constant context compactions and crashes |
| Qwen3-vl-8b | Local | Not tested (vision model, wrong use case) |
| Qwen3.5 27B 4bit | Local | Stuck reading AGENTS.md (did not finish after 40 minutes) |
| Qwen3.5 9B | Local | Stuck reading AGENTS.md |
| Gemini 3.1 Flash Lite | Requesty | Failed to call tools consistently |
| DeepSeek V3.2 | Requesty | Failed to create a plan; 0/40 on implementation |

---

## Recommendations

### Best Overall: Claude Sonnet 4.6 (GitHub Copilot)

With a flat GitHub Copilot subscription, Sonnet 4.6 delivers the second-best planning score (35/40) and the best implementation score (33/40) in just 10 minutes. It self-corrects style issues (e.g., Lombok vs records), reads the codebase accurately, and produces fully working code. At a flat subscription price shared across all usage, this is the clearest recommendation for a hobbyist.

### Best Value Cloud: MiniMax M2.5 (Requesty)

For pure pay-per-use cost, MiniMax M2.5 is the most economical option that fully implements the feature: ~$0.02 for planning and ~$0.24 for a complete implementation (25/40). The score is lower than the top tier but the feature was fully functional.

### Best Local Option: Nemotron 3 Nano 30B

No local model came close to matching the cloud models. Nemotron 3 Nano 30B was the best local performer (13/40) but took 54 minutes and stopped without feedback. **Local models on a Mac M4 Mini 32 GB are not viable for this kind of structured, multi-file Spring Boot feature development** — they lack the context handling, tool-calling reliability, and instruction-following needed.

### Avoid (Poor Quality/Value)

- **Devstral (Requesty)**: Most expensive cloud option (~$2.46 for implementation) with mediocre results (24/40).
- **GLM 4.7**: Strong planning (34/40) but the $0.25 planning cost + $0.52 implementation cost adds up, with results slightly behind Sonnet.
- **Qwen Turbo**: Cheapest cloud option but consistently failed at tool calling — not usable.
- **All local models**: Unreliable tool calling, wrong Java style, very slow.

### Summary Table

| Model | Total Cost (plan + impl) | Impl Score | Verdict |
|-------|--------------------------|-----------|---------|
| Claude Sonnet 4.6 | subscription | 33/40 | **Best choice** |
| Claude Haiku 4.5 | subscription | 31/40 | Good, cheaper subscription tier |
| Claude Opus 4.6 | subscription | 26/40 | Surprisingly worse impl than Sonnet |
| MiniMax M2.5 | ~$0.26 | 25/40 | **Best pay-per-use value** |
| Kimi K2.5 | ~$1.84 | 25/40 | Fast but expensive |
| GLM 4.7 | ~$0.77 | 29/40 | Good quality, moderate cost |
| Devstral (Requesty) | ~$2.47 | 24/40 | Expensive for mediocre results |
| Nemotron 3 Nano 30B | free | 13/40 | Best local, still not usable |
| Other local models | free | ≤5/40 | Not viable |

---

## Repository Structure

```
.
├── README.md                        # This file
├── AGENTS.md                        # AI assistant instructions for this codebase
├── devices-feature.md               # Baseline implementation plan (Claude Haiku 4.5)
├── devices-plan-evaluation.md       # Detailed planning phase evaluation
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
