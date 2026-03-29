# LLM Evaluation — Hobby Coding on Mac M4 Mini

This repository evaluates the practical usefulness of local and cloud LLMs for hobby software development on a **Mac M4 Mini with 32 GB RAM**. 
The goal is to find a suitable setup for everyday feature development given real-world constraints: cost, speed, and code quality.

## TL;DR

**Best setup**: Use a [GitHub Copilot Pro subscription](https://github.com/features/copilot/plans) ($10/month) with **Claude Sonnet 4.6** (~$0.03/prompt) or **Claude Opus 4.6** (~$0.09/prompt, 3× Sonnet cost) for planning and **GPT 5.4** (~$0.03/prompt) for implementation.
GPT 5.4 is the top implementation scorer and also plans competently (31/40), but Claude models remain clearly better planners.
Sonnet 4.6 remains the best single-model workflow (36/40 planning, 36/40 implementation) if you prefer to keep everything in one model.

**No subscription**: Use [Requesty](https://requesty.ai) with **GLM 4.7** for planning and **MiniMax M2.5** for implementation at ~$0.49/feature — the cheapest pay-per-use combination that produces a fully functional result (34/40 plan, 28/40 implementation). A Copilot Pro subscription is cheaper per feature if you implement more than ~21 features/month.

**Local models are insufficient for this workflow**: No local model on a Mac M4 Mini 32 GB produced a fully working result under these conditions. 
The best local implementation scored 13/40 and took 54 minutes. 
Simpler tasks, smaller context, or better model/hardware fit may yield different results — but that is out of scope for this evaluation.

### Tier Overview

| Tier | Models | Hosted by | Plan | Impl | Cost per feature | Time |
|------|--------|-----------|:----:|:----:|:----------------:|:----:|
| **Recommended** | Claude Sonnet 4.6 | GitHub Copilot | 36 | 36 | $10/mo · ~$0.07/feature † | ~12m |
| **Recommended** | Claude Opus 4.6 | GitHub Copilot | 38 | 32 | $10/mo · ~$0.20/feature † | ~15m |
| **Recommended** | GPT 5.4 | GitHub Copilot | 31 | 37 | $10/mo · ~$0.07/feature † | ~10m |
| **Strong alternative** | GLM 4.7 | Requesty | 34 | 35 | ~$2.71 | ~15m |
| **Viable** | GPT 5.4 | ChatGPT Plus | 29 | 35 | $20/mo · ~$0.05/feature ‡ | ~10m |
| **Viable** | GPT 5.3 Codex | ChatGPT Plus | 30 | 34 | $20/mo · ~$0.05/feature ‡ | ~11m |
| **Viable** | GPT 5.3 Codex Extra-High | ChatGPT Plus | 31 | 34 | $20/mo · ~$0.10/feature ‡ | ~23m |
| **Viable** | GPT 5.3 Codex High | ChatGPT Plus | 26 | 34 | $20/mo · ~$0.05/feature ‡ | ~16m |
| **Viable** | GPT 5.3 Codex Low | ChatGPT Plus | 25 | 33 | $20/mo · ~$0.03/feature ‡ | ~10m |
| **Viable** | Claude Haiku 4.5 | GitHub Copilot | 25 | 31 | $10/mo · ~$0.02/feature † | ~14m |
| **Viable** | Kimi K2.5 | Requesty | 30 | 29 | ~$1.84 | ~7m |
| **Viable** | MiniMax M2.5 | Requesty | 27 | 28 | ~$0.26 | ~14m |
| **Not recommended** | Devstral | Requesty | 26 | 25 | free* | ~28m |
| **Not recommended** | All local models | Local | 9-14 | 5-13 | free | 18-54m |
| **Not recommended** | Qwen Turbo, DeepSeek-V3.2 | Requesty | 8-12 | 4-8 | <$0.60 | 12m+ |

> † GitHub Copilot Pro ($10/month) includes 300 premium requests. Haiku costs 0.33 requests (~$0.01); Sonnet and GPT 5.4 cost 1 request each (~$0.03); Opus costs 3 requests (~$0.09). A same-model two-prompt workflow costs about ~$0.02 for Haiku, ~$0.07 for Sonnet or GPT 5.4, and ~$0.20 for Opus. Additional requests are $0.04 each.
>
> ‡ ChatGPT Plus ($20/month) is the subscription that provides access to GPT 5.4 and the GPT 5.3 Codex model family (OpenAI's coding-optimised model line, also branded as the "Codex" feature in ChatGPT). It uses daily and weekly usage caps rather than token pricing. Estimated costs are derived from cap percentages (plan + implementation combined). See [ChatGPT Plus details](#cloud-via-openais-chatgpt-plus-subscription) for daily/weekly cap breakdown.
>
> \* Devstral was free when the evaluation started. Requesty changed this during the evaluation.
>

---

## Table of Contents

- [Goal](#goal)
- [Setup](#setup)
- [Evaluation Task](#evaluation-task)
- [Scoring](#scoring)
- [Models Evaluated](#models-evaluated)
- [Results: Planning Phase](#results-planning-phase)
- [Results: Implementation Phase](#results-implementation-phase)
- [Time Summary](#time-summary)
- [Cost Guidance](#cost-guidance)
- [Models That Failed Early](#models-that-failed-early)
- [Recommendations](#recommendations)
- [Repository Structure](#repository-structure)

---

## Goal

To answer the question: **Which LLMs are well suited for a hobby programmer using a Mac M4 Mini?**

The evaluation focuses on:

- **Local models** — run entirely on-device via LM Studio (no API costs, private, but limited by hardware)
- **Cloud models** — accessed via GitHub Copilot, ChatGPT (subscription based) and [Requesty](https://requesty.ai) (pay-per-use)

The task is a realistic feature request in a non-trivial Spring Boot codebase with strict architectural rules defined in `AGENTS.md`.

---

## Setup

**Hardware**: Apple Mac M4 Mini, 32 GB unified memory

**Tool**: [OpenCode](https://opencode.ai) — AI coding agent for the terminal

**Local model runtime**: LM Studio

**Cloud access**: GitHub Copilot, ChatGPT and Requesty

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

## Scoring

Both phases were scored on **4 criteria, 10 points each** (40 points maximum).

### Planning Phase Criteria

| # | Criterion | What it measures |
|---|-----------|------------------|
| 1 | AGENTS.md Compliance | How well the plan follows the rules in `AGENTS.md` and linked pattern docs |
| 2 | Codebase Fit | Alignment with existing codebase (entities, migration style, `PageResult`, `RepositoryIT`, etc.) |
| 3 | Completeness | Whether all necessary steps are included (migration, entity, repo, service, DTOs, controller, config, tests, AGENTS.md update) |
| 4 | Detail Level | How concrete and actionable each step is (code snippets, field lists, method signatures, file paths) |

### Implementation Phase Criteria

| # | Criterion | What it measures |
|---|-----------|------------------|
| 1 | Completeness | All required artifacts present and functional (migration, entity, repo, service, DTOs, controller, config, tests) |
| 2 | Test Coverage | JSON model tests, repository ITs, service unit tests, controller tests — all present, passing, and meaningful |
| 3 | Compliance | Follows AGENTS.md rules: `@Configuration` wiring, package-protected repo, layering, GDPR logging |
| 4 | Code Quality | Naming, imports, efficient queries, correct ID generation, correct types, migration filename format, DTO transformation placement (mapping belongs in the DTO, not the controller) |

> See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) and [`devices-implementation-evaluation.md`](devices-implementation-evaluation.md) for the full scoring breakdown per model.

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

[GitHub Copilot](https://github.com/features/copilot/plans) offers multiple subscription tiers. For this evaluation, the relevant plans are:

| Plan | Monthly cost | Premium requests included | Extra requests |
|------|:------------:|:-------------------------:|:--------------:|
| Pro | $10 | 300 | $0.04/request |
| Pro+ | $39 | 1,500 | $0.04/request |

Each model consumes a different number of premium requests per prompt:

| Model | Premium requests per prompt |
|-------|:---------------------------:|
| Claude Haiku 4.5 | 0.33 |
| Claude Sonnet 4.6 | 1 |
| Claude Opus 4.6 | 3 |
| GPT 5.4 | 1 |

### Cloud via ChatGPT Plus subscription

[ChatGPT Plus](https://openai.com/chatgpt/pricing/) costs $20/month and includes access to GPT models at multiple thinking levels. 
Unlike token-based pricing, ChatGPT Plus uses **daily and weekly usage caps** — each task consumes a percentage of your daily/weekly budget.

| Model | Thinking | Plan cap (daily) | Impl cap (daily) | Est. cost/feature | Plan / Impl |
|-------|----------|:----------------:|:-----------------:|:-----------------:|:-----------:|
| GPT 5.4 | Default | ~1% | ~7% | ~$0.05 | 29 / 35 |
| GPT 5.3 Codex | Default | ~1% | 6% | ~$0.05 | 30 / 34 |
| GPT 5.3 Codex | Low | <1% | 4% | ~$0.03 | 25 / 33 |
| GPT 5.3 Codex | High | ~1% | 8% | ~$0.05 | 26 / 34 |
| GPT 5.3 Codex | Extra-High | ~2% | 12% | ~$0.10 | 31 / 34 |

**Cost calculation**: Estimated cost per feature combines plan + implementation and uses the weekly cap as the binding constraint. 
Based on the numbers per task, the weekly limit will be hit after hitting the daily cap ~4 times in a row.

**Impact for hobby coders**: The daily cap is the practical constraint. 
At the default thinking level (6-7% daily for implementation), you can run ~14-16 implementation tasks per day. 
The weekly cap only becomes binding if you consistently code every day. 
At extra-high thinking (12% daily), you're limited to ~8 tasks per day, which may feel constraining during intense development sessions.

**Note**: GPT 5.4 Extra-High thinking was tested for planning only (29/40) — it matched the default GPT 5.4 result at 4× the time and 2× the daily cap cost, confirming that higher thinking does not improve planning for our case.

### Cloud via Requesty (pay-per-use)

[Requesty](https://www.requesty.ai/) is a gateway to different LLM providers similar to [OpenRouter](https://openrouter.ai/).
Requesty was picked to fulfill the side goal of finding an European alternative to OpenRouter.
The pricing is based on input and output tokens. 
The table below shows the **actual cost observed per evaluation run** (planning + implementation) rather than raw token prices.

| Model | Provider | Planning cost | Implementation cost | Total per feature |
|-------|----------|:-------------:|:-------------------:|:-----------------:|
| GLM 4.7 | Zhipu AI (via Nebius) | ~$0.25 | ~$2.46 | **~$2.71** |
| Kimi K2.5 | Moonshot AI (via Nebius) | ~$0.10 | ~$1.74 | **~$1.84** |
| DeepSeek-V3.2 | DeepSeek | ~$0.05 | ~$0.52 (aborted) | **~$0.57** |
| Devstral | Mistral AI | ~$0.01 | free* | **~$0.01** |
| MiniMax M2.5 | MiniMaxAI | ~$0.02 | ~$0.24 | **~$0.26** |
| Qwen Turbo | Alibaba | <$0.01 | ~$0.16 | **~$0.17** |

**Notes**: Devstral was free when starting the evaluation. 
Requesty changed this during the evaluation.
GLM 4.7 has been tested instead of GLM 5 since it was available on a European provider.

### Models That Failed Early

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

## Results: Planning Phase

The planning prompt asked each model to produce a structured implementation plan.

| Model | Type | Time | Cost | Plan Score |
|-------|------|------|------|:----------:|
| **Claude Opus 4.6** | GitHub Copilot | 3m 8s | ~$0.09 † | **38/40** |
| **Claude Sonnet 4.6** | GitHub Copilot | 2m 23s | ~$0.03 † | **36/40** |
| **GLM 4.7** | Requesty | 2m 57s | ~$0.25 | **34/40** |
| **GPT 5.3 Codex Extra-High** | ChatGPT Plus | 2m 37s | ~$0.02 ‡ | **31/40** |
| **GPT 5.4 (GitHub Copilot)** | GitHub Copilot | 45s | ~$0.03 † | **31/40** |
| **GPT 5.3 Codex** | ChatGPT Plus | 1m 10s | ~$0.01 ‡ | **30/40** |
| **Kimi K2.5** | Requesty | 35s | ~$0.10 | **30/40** |
| **GPT 5.4 (ChatGPT)** | ChatGPT Plus | 42s | ~$0.01 ‡ | **29/40** |
| **MiniMax M2.5** | Requesty | 1m 37s | ~$0.02 | **27/40** |
| **GPT 5.3 Codex High** | ChatGPT Plus | 1m 56s | ~$0.01 ‡ | **26/40** |
| **Devstral** | Requesty | 17s | ~$0.01 | **26/40** |
| **GPT 5.3 Codex Low** | ChatGPT Plus | 43s | <$0.01 ‡ | **25/40** |
| **Claude Haiku 4.5** *(baseline)* | GitHub Copilot | 35s | ~$0.01 † | **25/40** |
| **Nemotron 3 Nano 30B** | Local | 1m 42s | free | **14/40** |
| **DeepSeek-V3.2** | Requesty | 42s | $0.05 | **12/40** |
| **Devstral Small 2** | Local | 1m 28s | free | **12/40** |
| **GPT-OSS Safeguard 20B** | Local | 2m 41s | free | **9/40** |
| **Qwen Turbo** | Requesty | 58s | <$0.01 | **8/40** |

### Key Takeaways — Planning

**Top tier (34-38/40) — Claude + GLM**: Opus (38) and Sonnet (36) remain clearly ahead on codebase fit, architectural reasoning, and implementation-ready detail. Only Opus and Sonnet explicitly address both DTO mapping directions with concrete code: incoming `toEntity()` on the DTO and an outgoing controller `toDto()` helper — and both do so without any DTO leaking into the service layer. GLM 4.7 (34, ~$0.25) is the best pay-per-use planner — the most complete Java code examples and the only plan to include `@EntityGraph` for N+1 prevention.

**Middle tier (25-31/40) — GPT models**: GPT plans are fast but less grounded in project conventions — most skip keyset pagination, `PageResult`, and DTO-owned `toEntity()` mapping. GPT 5.3 Codex Extra-High (31) edges ahead with the most schema-specific migration; GPT 5.4 Copilot also scores 31; GPT 5.3 Codex Default scores 30; GPT 5.4 (ChatGPT) scores 29 — GPT 5.4 Extra-High matched this exactly at 4× the time, confirming higher thinking does not improve GPT 5.4 planning. GPT 5.3 Codex Low (25) drops to the Haiku baseline.

**Not viable (≤14/40) — Local and weak cloud**: The best local plan (Nemotron, 14) falls below even the weakest cloud baseline (Haiku, 25). Local models consistently miss SQL schemas, `@Configuration` wiring, JSON model tests, and GDPR logging. DeepSeek invented requirements (optimistic locking, soft deletes) that would derail implementation.

> See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Results: Implementation Phase

All models implemented the feature using the Claude Haiku 4.5 plan as a shared baseline.

| Model | Type | Time | Cost | Impl Score |
|-------|------|------|------|:----------:|
| **GPT 5.4 (Copilot)** | GitHub Copilot | 9m | ~$0.03 † | **37/40** |
| **Claude Sonnet 4.6** | GitHub Copilot | 10m | ~$0.03 † | **36/40** |
| **GPT 5.4 (ChatGPT)** | ChatGPT Plus | 9m | ~$0.04 ‡ | **35/40** |
| **GLM 4.7** | Requesty | 12m | ~$2.46 | **35/40** |
| **GPT 5.3 Codex Extra-High** | ChatGPT Plus | 20m | ~$0.08 ‡ | **34/40** |
| **GPT 5.3 Codex** | ChatGPT Plus | 10m | ~$0.04 ‡ | **34/40** |
| **GPT 5.3 Codex High** | ChatGPT Plus | 14m | ~$0.04 ‡ | **34/40** |
| **GPT 5.3 Codex Low** | ChatGPT Plus | 9m | ~$0.02 ‡ | **33/40** |
| **Claude Opus 4.6** | GitHub Copilot | 12m | ~$0.09 † | **32/40** |
| **Claude Haiku 4.5** | GitHub Copilot | 13m | ~$0.01 † | **31/40** |
| **Kimi K2.5** | Requesty | 6m | ~$1.74 | **29/40** |
| **MiniMax M2.5** | Requesty | 12m | ~$0.24 | **28/40** |
| **Devstral** | Requesty | 28m | free* | **25/40** |
| **Devstral Small 2** | Local | 54m | free | **13/40** |
| **Nemotron 3 Nano 30B** | Local | 54m | free | **12/40** |
| **DeepSeek-V3.2** | Requesty | aborted (6m) | ~$0.52 | **8/40** |
| **GPT-OSS Safeguard 20B** | Local | 18m | free | **5/40** |
| **Qwen Turbo** | Requesty | 12m | ~$0.16 | **4/40** |

### Key Takeaways — Implementation

- **GPT 5.4 leads implementation at 37/40** on Copilot, followed by Sonnet 4.6 at 36/40. The pick between GPT 5.4 and Sonnet should be on use case — GPT 5.4 on Copilot for richest JPA model and code quality; Sonnet for the architecturally cleanest layering.
- **Sonnet 4.6 is the cleanest architectural implementation at 36/40** — the only model that validates cross-feature sensors through `SensorService` as required by the layering rules, with strong completeness and 9/10 across three criteria.
- **GLM 4.7 is the best pay-per-use implementation at 35/40** — richest JPA sensor graph and strong compliance, though missing `@GeneratedValue` and Flyway schema misconfiguration prevent ITs from passing.
- **GPT 5.4 (ChatGPT)** ties GLM at 35/40. Near-identical architecture to the Copilot run; the score difference vs. Copilot reflects a DTO-in-service leak and missing `@GeneratedValue`.
- **GPT 5.3 Codex variants form a strong middle cluster** — Default, High, and Extra-High all land at 34/40, so higher thinking levels bring little implementation benefit. Pick based on pricing.
- **Haiku and Kimi are workable but flawed** — Haiku returns empty sensor lists despite the relationship being present, while Kimi ships a serious `findAll().stream().filter()` performance bug.
- **Local models are not viable for this workflow** — the best local implementation scored 13/40 and still failed to produce a complete working feature.

> See [`devices-implementation-evaluation.md`](devices-implementation-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Time Summary

Time measured is wall-clock time from prompt submission to completion, including all tool calls, file reads, and self-corrections.

### Planning Phase

| Speed | Model | Time | Notes |
|-------|-------|:----:|-------|
| Fast | Devstral | 17s | Fastest overall, but lower quality (26/40) |
| Fast | Kimi K2.5 | 35s | Good speed-to-quality ratio |
| Fast | Claude Haiku 4.5 | 35s | Fastest subscription model |
| Fast | GPT 5.4 (ChatGPT) | 42s | Fast GPT planning result (29/40) |
| Fast | GPT 5.3 Codex Low | 43s | Fast but baseline-level planning (25/40) |
| Fast | GPT 5.4 (Copilot) | 45s | Fast GPT planning result (31/40) |
| Medium | Qwen Turbo | 58s | Fast but low quality (8/40) |
| Medium | GPT 5.3 Codex | 1m 10s | Strongest ChatGPT planning result (30/40) |
| Medium | Devstral Small 2 (local) | 1m 28s | Slow for a local model |
| Medium | MiniMax M2.5 | 1m 37s | Decent speed for the quality |
| Medium | Nemotron 3 Nano (local) | 1m 42s | Slow with mediocre output |
| Medium | GPT 5.3 Codex High | 1m 56s | Lower planning quality than default thinking level (26/40) |
| Slow | Claude Sonnet 4.6 | 2m 23s | Worth the wait (36/40) |
| Slow | GPT 5.3 Codex Extra-High | 2m 37s | Best schema detail of any GPT plan but expensive at 2× daily cap (31/40) |
| Slow | GPT-OSS Safeguard (local) | 2m 41s | Slowest for worst result (9/40) |
| Slow | GLM 4.7 | 2m 57s | Worth the wait (34/40) |
| Slow | Claude Opus 4.6 | 3m 8s | Slowest, but best plan (38/40) |

### Implementation Phase

| Speed | Model | Time | Notes |
|-------|-------|:----:|-------|
| Fast | Kimi K2.5 | 6m | Fastest cloud implementation |
| Fast | GPT 5.3 Codex Low | 9m | Best GPT value; full sensor details |
| Fast | GPT 5.4 (Copilot) | 9m | Top scorer at 37/40; fastest high-quality result |
| Fast | GPT 5.4 (ChatGPT) | 9m | 35/40; same speed, near-identical architecture |
| Medium | Claude Sonnet 4.6 | 10m | Best Anthropic implementation; correct layering; 36/40 |
| Medium | GPT 5.3 Codex | 10m | Default thinking; 34/40 |
| Medium | Claude Opus 4.6 | 12m | Slightly slower than Sonnet, lower score |
| Medium | GLM 4.7 | 12m | Best pay-per-use implementation; 35/40 |
| Medium | MiniMax M2.5 | 12m | Decent for the low cost |
| Medium | Qwen Turbo | 12m | 12 minutes wasted (4/40) |
| Medium | Claude Haiku 4.5 | 13m | Needed a follow-up prompt |
| Medium | GPT 5.3 Codex High | 14m | Same score as default thinking, 40% slower |
| Slow | GPT-OSS Safeguard (local) | 18m | Needed constant prompting |
| Slow | GPT 5.3 Codex Extra-High | 20m | Same score as default thinking for double the time |
| Slow | Devstral | 28m | Too slow for the quality |
| Very slow | Devstral Small 2 (local) | 54m | Nearly an hour for an incomplete result |
| Very slow | Nemotron 3 Nano (local) | 54m | Nearly an hour, stopped on its own |

### Best time-to-quality ratio

For a complete plan + implementation cycle, including model combinations across access paths:

| Workflow (Plan, Implement) | Total time | Combined score | Est. cost/feature | Access |
|----------------------------|:----------:|:--------------:|:-----------------:|--------|
| Opus → GPT 5.4 | ~12m | 75/80 | ~$0.12 † | Copilot |
| Sonnet → GPT 5.4 | ~12m | 73/80 | ~$0.07 † | Copilot |
| Sonnet → Sonnet | ~12m | 72/80 | ~$0.07 † | Copilot |
| GLM 4.7 → GLM 4.7 | ~15m | 69/80 | ~$2.71 | Requesty |
| GPT 5.4 → GPT 5.4 | ~10m | 68/80 | ~$0.07 † | Copilot |
| GPT 5.4 → GPT 5.4 | ~10m | 64/80 | ~$0.05 ‡ | ChatGPT |
| GPT 5.3 Codex → GPT 5.3 Codex | ~11m | 64/80 | ~$0.05 ‡ | ChatGPT |
| GLM 4.7 → MiniMax M2.5 | ~15m | 62/80 | ~$0.49 | Requesty |
| Kimi K2.5 → Kimi K2.5 | ~7m | 59/80 | ~$1.84 | Requesty |
| Haiku → Haiku | ~14m | 56/80 | ~$0.02 † | Copilot |

GPT models are available on both Copilot and ChatGPT. 
Since all GPT models are also accessible through Copilot (at lower cost and without usage caps), the ChatGPT rows are mainly relevant if you spend many prompts on planning or already subscribe for other ChatGPT features.

---

## Cost Guidance

### Subscription vs. Pay-Per-Use: When Does Each Make Sense?

The key question for hobby coders: **which access path offers the best value — GitHub Copilot, ChatGPT Plus, or pay-per-use via Requesty?**

#### GitHub Copilot vs. ChatGPT Plus — Platform Comparison

Both are subscription services, but they differ significantly in model access, pricing structure, and value for coding:

| | GitHub Copilot Pro ($10/mo) | ChatGPT Plus ($20/mo) |
|---|---|---|
| **Model families** | Claude (Haiku, Sonnet, Opus) + GPT (5.4) + others | OpenAI models only (GPT 5.4, 5.3 Codex) |
| **Pricing model** | Premium requests (300/mo, ~$0.03 each) | Daily/weekly usage caps |
| **Usage limits** | No daily/weekly caps; only total request count | Daily cap (~14-16 impl tasks/day at default) |
| **Best planning** | Claude Sonnet 36/40, Opus 38/40 | GPT 5.3 Codex Extra-High 31/40 |
| **Best implementation** | GPT 5.4 37/40, Sonnet 34/40 | GPT 5.4 35/40, GPT 5.3 Codex 34/40 |
| **Monthly cost** | $10 | $20 |

**Key differences for coding workflows**:
- **Copilot's main advantage is Claude access for planning** — Sonnet (36/40) and Opus (38/40) significantly outperform all GPT planning results (25-31/40). ChatGPT has no equivalent.
- **Implementation costs are similar** — GPT 5.4 costs ~$0.03 on Copilot vs. ~$0.04 on ChatGPT per task, but Copilot has no daily caps.
- **ChatGPT can have cheaper planning per task** (~$0.01 vs. ~$0.03 for Copilot Sonnet), but the $20/mo base fee is double and GPT planning quality is lower. This only pays off if you already subscribe for non-coding ChatGPT features.
- **Copilot is the better dedicated coding subscription** — half the cost, higher combined quality, no usage caps, and access to both Claude and GPT model families.

#### GitHub Copilot Pro ($10/month)

GitHub Copilot Pro costs $10/month and includes **300 premium requests**.
Each request costs $10 / 300 = **\~$0.033**.
The amount of requests consumed depend on the [model](https://docs.github.com/en/copilot/concepts/billing/copilot-requests#model-multipliers).
Haiku consumes **0.33 requests** per prompt (~$0.01); Sonnet and GPT 5.4 consume **1 request** (~$0.03); Opus consumes **3 requests** (~$0.09).
This evaluation used **2 prompts per feature** (plan + implementation).

| Workflow | Requests per feature | Cost per feature |
|----------|:--------------------:|:----------------:|
| Opus plan + GPT 5.4 impl | 4 | ~$0.12 |
| Sonnet plan + GPT 5.4 impl | 2 | ~$0.06 |
| Opus plan + Haiku impl | 3.33| ~$0.10 |
| Haiku plan + Haiku impl | 0.66 | ~$0.02 |

Additional requests beyond 300 are billed at **$0.04 each**.

#### Break-even: Subscription vs. Requesty

The best pay-per-use option by quality is **GLM 4.7** at ~$2.71/feature.
The cheapest viable option is **MiniMax M2.5** at ~$0.26/feature.

The break-even point is the number of features at which the $10 subscription fee is recovered compared to Requesty.
Below that number, pay-per-use is cheaper; above it, the subscription wins.

| Comparison | Copilot Pro cost/feature | Requesty cost/feature | Break-even |
|------------|:------------------------:|:---------------------:|:----------:|
| Opus plan + GPT 5.4 impl vs GLM 4.7 | ~$0.12 | ~$2.71 | **~4 features** |
| Opus plan + GPT 5.4 impl vs GLM plan + MiniMax impl | ~$0.12 | ~$0.49 | **~21 features** |
| Opus plan + GPT 5.4 impl vs MiniMax M2.5 | ~$0.12 | ~$0.26 | **~39 features** |

**Bottom line**: If you implement more than ~4 features per month using GLM 4.7, a **Copilot Pro subscription is cheaper** — and delivers higher-quality results.
For most hobby coders doing any regular development, that threshold is trivial to cross.

#### When Pay-Per-Use Makes Sense

- You code infrequently
- You want to avoid a recurring subscription
- You prefer a European provider (Requesty routes through Nebius for some models)
- You want to experiment with different models without commitment

#### GitHub Copilot Pro+ ($39/month)

Pro+ costs $39/month and includes 1,500 premium requests (~$0.026/request).
At 4 requests/feature with Opus + GPT 5.4, that covers up to 325 features/month.
**Only consider Pro+ if you heavily use Opus** (3 requests/prompt) and prompt a lot on a daily basis.

#### ChatGPT Plus ($20/month)

ChatGPT Plus costs $20/month and provides access to GPT 5.4 and 5.3 Codex. 
Unlike token-based or request-based pricing, ChatGPT Plus uses **daily and weekly usage caps** — each task consumes a percentage of your budget.

| Model | Thinking | Est. cost/feature | Impl tasks/day | Plan / Impl |
|-------|----------|:-----------------:|:--------------:|:-----------:|
| GPT 5.4 | Default | ~$0.05 | ~14 | 29 / 35 |
| GPT 5.3 Codex | Low | ~$0.03 | ~25 | 25 / 33 |
| GPT 5.3 Codex | Default | ~$0.05 | ~16 | 30 / 34 |
| GPT 5.3 Codex | High | ~$0.05 | ~12 | 26 / 34 |
| GPT 5.3 Codex | Extra-High | ~$0.10 | ~8 | 31 / 34 |

**Key insight for hobby coders**: The daily cap is the practical constraint. At the default thinking level, ~14-16 tasks per day is ample for hobby coding. 
The weekly cap only matters if you code intensively every day.

**Diminishing returns on thinking**: For implementation, Default, High, and Extra-High all scored 34/40 while Low scored 33/40. For planning, Extra-High (31/40) edges out Default (30/40) but at 2× the daily cap cost. GPT 5.4 Extra-High planning matched the default GPT 5.4 result (29/40) at 4× the time — **the default thinking level is the best value overall**.

**ChatGPT Plus vs. GitHub Copilot Pro**: Copilot Pro costs half as much ($10/mo vs. $20/mo), has no daily or weekly usage caps, and provides access to Claude models for planning (which score 36-38/40 vs. GPT's 29-31/40).
The score differences between GPT 5.4 on Copilot (37/40 impl) and ChatGPT (35/40 impl) are likely non-deterministic — the pick should be on pricing and platform features.

**ChatGPT Plus has one practical advantage for planning-heavy workflows**: at ~1% daily cap per planning prompt, you can run ~100 planning prompts per day essentially for free within the subscription — making it well-suited for iterative, interactive planning sessions with many back-and-forth prompts. On Copilot, each GPT 5.4 planning prompt costs 1 premium request (~$0.03), so an interactive session with 10+ prompts visibly erodes the 300-request monthly budget. If your workflow involves extended planning conversations rather than a single-shot plan, ChatGPT Plus removes that friction at the cost of lower planning quality (29-31/40 vs. 36-38/40 on Copilot with Claude).

**Note**: GPT 5.4 requires OpenCode 1.3 or later.

---

## Recommendations

Rather than looking for a single best model, the more useful question is: **which model is best for each role in your workflow?** Planning and implementation are different tasks — a model that writes excellent plans does not necessarily write the best code, and vice versa.

### Planning: Claude Opus 4.6 or Claude Sonnet 4.6 (GitHub Copilot)

Opus produces the best plans (38/40), with the strongest architectural reasoning, explicit cross-cutting concerns, and the most accurate codebase alignment. 
Sonnet follows closely (36/40) with the cleanest API design and most complete configuration examples.
With Opus consuming three times more credits, it is worth sticking with Sonnet for features with low or medium complexity.

For pay-per-use, **GLM 4.7** is the best cloud planner at ~$0.25 per plan (34/40), with detailed Java code examples and a rich test plan.

### Implementation: GPT 5.4 or Claude Sonnet 4.6 (GitHub Copilot)

**GPT 5.4** scored 35-37/40 across access paths (37 on Copilot, 35 on ChatGPT) — the richest JPA model, full sensor details, DTO-owned transformation, comprehensive tests, and completes in ~9 minutes. 
The score difference between Copilot and ChatGPT is likely non-deterministic; pick based on pricing (Copilot at $10/mo vs. ChatGPT at $20/mo with usage caps).

**Sonnet 4.6** scores 36/40 — the only model that correctly used `SensorService` for cross-feature validation, the architecturally cleanest implementation. 
It completes in ~10 minutes on the same Copilot subscription and is the better single-model choice when architectural correctness matters most.

For pay-per-use implementation, **GLM 4.7** (~$2.46, 35/40) is the best non-subscription option.

### Recommended Setup

**Subscription (GitHub Copilot Pro — $10/month):**
Use **Opus 4.6** or **Sonnet 4.6** to produce the plan, review and adjust it, then use **GPT 5.4** or **Sonnet 4.6** to implement. 
You get 300 premium requests/month, enough for ~75-150 features depending on model choice.
**Sonnet 4.6** (36/40 plan, 36/40 implementation) is the better single-model choice when architectural correctness matters most.

**ChatGPT Plus ($20/month):**
GPT 5.3 Codex planning and implementation is respectable at the default thinking level (30/40 plan, 34/40 implementation), but still trails Copilot-based Claude planning and GPT 5.4 implementation. 
Higher thinking levels show diminishing returns: Extra-High edges ahead in planning (31/40) but at 2× the daily cap, while Low drops to baseline quality (25/40). 
**ChatGPT Plus is worth considering if your workflow involves iterative, interactive planning** — planning prompts cost only ~1% of the daily cap, so extended back-and-forth planning sessions are effectively unlimited within the subscription. 
On Copilot, each Sonnet prompt costs 1 premium request, which adds up quickly in interactive sessions. The tradeoff is lower planning quality (29-31/40 vs. 36-38/40 with Claude on Copilot) and a higher base subscription cost ($20/mo vs. $10/mo).

**Pay-per-use (Requesty):**
Use **GLM 4.7** for planning (~$0.25) and implementation (~$2.46). 
Total cost per feature: ~$2.71. The quality is strong but the implementation cost is high — at this price, a Copilot Pro subscription pays for itself after just 4 features.

**Best value hybrid:**
Use **GLM 4.7** for planning (~$0.25 via Requesty) and **MiniMax M2.5** for implementation (~$0.24). Total ~$0.49 per feature — the lowest cost for a fully functional result.

### Local Models

No local model came close to matching cloud models. 
Devstral Small 2 scored highest locally (13/40) but only produced the data layer — no service, controller, or DTOs. 
Nemotron 3 Nano 30B (12/40) took 54 minutes and stopped without feedback. Gemma 3 12B failed entirely due to constant tool call failures. 
**Local models on a Mac M4 Mini 32 GB are insufficient for this kind of structured, multi-file Spring Boot feature development** — they lack the context handling, tool-calling reliability, and instruction-following needed to follow strict architectural conventions and multi-file testing patterns — even in a single-feature codebase. It is worth exploring whether smaller, well-scoped tasks or lighter context requirements would yield better results, but that is out of scope for this evaluation.

### Models to Avoid

- **Qwen Turbo**: Cheapest cloud option but consistently failed at tool calling — not usable for either role.
- **DeepSeek-V3.2**: Aborted mid-implementation yet still cost ~$0.57 — poor value for any result.
- **Gemini 3.1 Flash Lite**: Failed completely; unable to call tools consistently.

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
| `gh-<model>/devices-plan` | GitHub Copilot model planning run |
| `gh-<model>/device-feature` | GitHub Copilot model implementation |
| `oai-<model>/devices-plan` | OpenCode with direct OpenAI API, or ChatGPT Plus model planning run |
| `oai-<model>/devices-feature` | OpenCode with direct OpenAI API, or ChatGPT Plus model implementation |
| `rq-<model>/devices-plan` | Requesty model planning run |
| `rq-<model>/devices-feature` | Requesty model implementation |
| `<model>/devices-plan` | Local model planning run |
| `<model>/devices-feature` | Local model implementation |

### Technologies

- **Java 21** with records, sealed classes, pattern matching
- **Spring Boot 3.x** with constructor injection and `@Configuration` beans
- **PostgreSQL 17** with Flyway migrations
- **Spring Data JPA** with keyset pagination
- **TestContainers** for repository integration tests
- **MockMvc** for controller tests
- **Springdoc OpenAPI** for API documentation
