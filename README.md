# LLM Evaluation — Hobby Coding on Mac M4 Mini

This repository evaluates the practical usefulness of local and cloud LLMs for hobby software development on a **Mac M4 Mini with 32 GB RAM**. 
The goal is to find a suitable setup for everyday feature development given real-world constraints: cost, speed, and code quality.

## TL;DR

**Best setup**: Use a [GitHub Copilot Pro subscription](https://github.com/features/copilot/plans) ($10/month) with **Claude Sonnet 4.6** for both planning and implementation. It scored highest on implementation among Anthropic models (35/40), produced the best plan after Opus (35/40), and completes a full feature in ~12 minutes across two prompts. **GPT 5.4** (also on Copilot Pro) is the top implementation scorer at 37/40.

**On a budget**: Use [Requesty](https://requesty.ai) with **GLM 4.7** for planning and **MiniMax M2.5** for implementation at ~$0.49/feature — the cheapest combination that produces a fully functional result (34/40 plan, 28/40 implementation).

**Local models are insufficient for this workflow**: No local model on a Mac M4 Mini 32 GB produced a fully working result under these conditions. The best local implementation scored 13/40 and took 54 minutes. Simpler tasks, smaller context, or better model/hardware fit may yield different results — but that is out of scope for this evaluation.

### Tier Overview

| Tier | Models | Plan | Impl | Cost per feature | Time |
|------|--------|:----:|:----:|:----------------:|:----:|
| **Recommended** | Claude Sonnet 4.6 | 35 | 35 | $10/mo · ~$0.03/prompt † | ~12m |
| **Recommended** | GPT 5.4 | — | 37 | $10/mo · ~$0.03/prompt † | ~9m ‡‡ |
| **Recommended** | Claude Opus 4.6 (planning only) | 36 | 32 | $10/mo · ~$0.09/prompt † | ~15m |
| **Strong alternative** | GLM 4.7 | 34 | 35 | ~$2.71 | ~15m |
| **Viable** | GPT 5.3 Codex Extra-High | — | 34 | $20/mo · ~$0.08/task ‡ | ~20m ‡‡ |
| **Viable** | GPT 5.3 Codex | — | 34 | $20/mo · ~$0.04/task ‡ | ~10m ‡‡ |
| **Viable** | GPT 5.3 Codex High | — | 34 | $20/mo · ~$0.04/task ‡ | ~14m ‡‡ |
| **Viable** | GPT 5.3 Codex Low | — | 33 | $20/mo · ~$0.02/task ‡ | ~9m ‡‡ |
| **Viable** | Claude Haiku 4.5 | 25 | 31 | $10/mo · ~$0.03/prompt † | ~14m |
| **Viable** | Kimi K2.5 | 29 | 29 | ~$1.84 | ~7m |
| **Viable** | MiniMax M2.5 | 27 | 28 | ~$0.26 | ~14m |
| **Not recommended** | Devstral (cloud) | 26 | 25 | free* | ~28m |
| **Not recommended** | All local models | 9-14 | 5-13 | free | 18-54m |
| **Not recommended** | Qwen Turbo, DeepSeek-V3.2 | 8-12 | 4-8 | <$0.60 | 12m+ |

> † GitHub Copilot Pro ($10/month) includes 300 premium requests. Haiku, Sonnet, and GPT 5.4 cost 1 request each (~$0.03); Opus costs 3 requests (~$0.09). Additional requests are $0.04 each.
>
> ‡ ChatGPT Plus ($20/month) with usage caps. Cost per task is estimated from the daily cap (see [Cost Guidance](#cost-guidance) for details). Planning evaluation pending for GPT models.
>
> ‡‡ Implementation time only (single prompt). GPT models were not evaluated for planning.
>
> \* Devstral was free when the evaluation started. Requesty changed this during the evaluation.

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
| 4 | Code Quality | Naming, imports, efficient queries, correct ID generation, correct types, migration filename format |

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

[Github Copilot](https://github.com/features/copilot/plans) offers multiple subscription tiers. For this evaluation, the relevant plans are:

| Plan | Monthly cost | Premium requests included | Extra requests |
|------|:------------:|:-------------------------:|:--------------:|
| Pro | $10 | 300 | $0.04/request |
| Pro+ | $39 | 1,500 | $0.04/request |

Each model consumes a different number of premium requests per prompt:

| Model | Premium requests per prompt |
|-------|:---------------------------:|
| Claude Haiku 4.5 | 1 |
| Claude Sonnet 4.6 | 1 |
| Claude Opus 4.6 | 3 |
| GPT 5.4 | 1 |

### Cloud via ChatGPT Plus (subscription)

[ChatGPT Plus](https://openai.com/chatgpt/pricing/) costs $20/month and includes access to GPT 5.3 Codex at multiple effort levels. Unlike token-based pricing, ChatGPT Plus uses **daily and weekly usage caps** — each task consumes a percentage of your daily/weekly budget.

| Model | Effort | Daily cap per task | Weekly cap per task | Est. cost per task |
|-------|--------|:------------------:|:-------------------:|:------------------:|
| GPT 5.3 Codex | Default | 6% | 2% | ~$0.04 |
| GPT 5.3 Codex Low | Low | 4% | 1% | ~$0.02 |
| GPT 5.3 Codex High | High | 8% | 2% | ~$0.04 |
| GPT 5.3 Codex Extra-High | Extra-High | 12% | 4% | ~$0.08 |

**Cost calculation**: Estimated cost per task uses the daily cap as the binding constraint: tasks/day = 100% / daily%, tasks/month = tasks/day × 30, cost/task = $20 / tasks/month. The weekly cap provides an additional ceiling that prevents binge-coding sessions from consuming a disproportionate share.

**Impact for hobby coders**: The daily cap matters most. At the default effort level (6% daily), you can run ~16 tasks per day — sufficient for most hobby coding sessions. The weekly cap (2% = 50 tasks/week) only becomes binding if you consistently code every day. At extra-high effort (12% daily), you're limited to ~8 tasks per day, which may feel constraining during intense development sessions.

**Note**: GPT 5.3 Codex models were only evaluated for implementation, not planning. They run as asynchronous background tasks in ChatGPT, not as interactive sessions like GitHub Copilot or Requesty.

### Cloud via Requesty (pay-per-use)

[Requesty](https://www.requesty.ai/) is a gateway to different LLM providers similar to [OpenRouter](https://openrouter.ai/).
Requesty was picked to fulfill the side goal of finding an European alternative to OpenRouter.
The pricing is based on input and output tokens. The table below shows the **actual cost observed per evaluation run** (planning + implementation) rather than raw token prices.

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
GLM 4.7 has been tested in favor of GLM 5 since it was available on a European provider.

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
| **Claude Opus 4.6** | GitHub | 3m 8s | subscription | **36/40** |
| **Claude Sonnet 4.6** | GitHub | 2m 23s | subscription | **35/40** |
| **GLM 4.7** | Requesty | 2m 57s | ~$0.25 | **34/40** |
| **Kimi K2.5** | Requesty | 35s | ~$0.10 | **29/40** |
| **MiniMax M2.5** | Requesty | 1m 37s | ~$0.02 | **27/40** |
| **Devstral** | Requesty | 17s | ~$0.01 | **26/40** |
| **Claude Haiku 4.5** *(baseline)* | GitHub | 35s | subscription | **25/40** |
| **Nemotron 3 Nano 30B** | Local | 1m 42s | free | **14/40** |
| **DeepSeek-V3.2** | Requesty | 42s | $0.05 | **12/40** |
| **Devstral Small 2** | Local | 1m 28s | free | **12/40** |
| **GPT-OSS Safeguard 20B** | Local | 2m 41s | free | **9/40** |
| **Qwen Turbo** | Requesty | 58s | <$0.01 | **8/40** |

### Key Takeaways — Planning

- **Opus and Sonnet dominate**: Both produce detailed, architecturally sound plans that closely follow the project's conventions. Opus excels at cross-cutting concerns; Sonnet has the cleanest API design.
- **GLM 4.7 is the best pay-per-use planner** at ~$0.25, providing the most complete Java code examples and the only plan to include `@EntityGraph` for N+1 prevention.
- **A significant gap separates cloud from local for this workflow**: The best local plan (Nemotron, 14/40) scores below even the weakest cloud plan baseline (Haiku, 25/40). For a task with this level of context and architectural constraints, local models consistently miss SQL schemas, `@Configuration` wiring, JSON model tests, and GDPR logging.
- **DeepSeek invented requirements** (optimistic locking, soft deletes) that would derail implementation — a model that adds scope unprompted is risky for structured workflows.

> See [`devices-plan-evaluation.md`](devices-plan-evaluation.md) for detailed per-model analysis and scoring breakdown.

---

## Results: Implementation Phase

All models implemented the feature using the Claude Haiku 4.5 plan as a shared baseline.

| Model | Type | Time | Cost | Impl Score | Notes |
|-------|------|------|------|:----------:|-------|
| **GPT 5.4** | GitHub | 9m | subscription | **37/40** | Rich JPA model (`@MapsId`, JOIN FETCH), full sensor details, `EntityManager.find` for sensors |
| **Claude Sonnet 4.6** | GitHub | 10m | subscription | **35/40** | Fully implemented, self-corrected Lombok usage, correct cross-feature layering |
| **GLM 4.7** | Requesty | 12m | ~$2.46 | **35/40** | Fully implemented, richest sensor response via JPA graph traversal |
| **GPT 5.3 Codex Extra-High** | ChatGPT+ | 20m | ~$0.08/task | **34/40** | Most thorough tests; `JpaSensorLookupRepository` layering violation |
| **GPT 5.3 Codex** | ChatGPT+ | 10m | ~$0.04/task | **34/40** | Solid baseline; returns sensor IDs only, JPQL sensor validation |
| **GPT 5.3 Codex High** | ChatGPT+ | 14m | ~$0.04/task | **34/40** | Named constraints, comprehensive tests; native SQL sensor validation |
| **GPT 5.3 Codex Low** | ChatGPT+ | 9m | ~$0.02/task | **33/40** | Full sensor details via `AssignedSensorDto`; sparse repo IT |
| **Claude Opus 4.6** | GitHub | 12m | subscription | **32/40** | Fully implemented, bypassed layering rule via DB exception catch |
| **Claude Haiku 4.5** | GitHub | 13m | subscription | **31/40** | Needed a second prompt to fix startup issues; sensor list always empty in response |
| **Kimi K2.5** | Requesty | 6m | ~$1.74 | **29/40** | Fully implemented, fastest cloud; critical `findAll().stream().filter()` performance bug |
| **MiniMax M2.5** | Requesty | 12m | ~$0.24 | **28/40** | Fully implemented; sensor names/types missing from response despite being mapped |
| **Devstral** | Requesty | 28m | free* | **25/40** | Fully implemented but sensor retrieval left as hardcoded empty list placeholder |
| **Devstral Small 2** | Local | 54m | free | **13/40** | Only data layer implemented; no service, controller, or DTOs |
| **Nemotron 3 Nano 30B** | Local | 54m | free | **12/40** | Very slow, stopped without feedback; wrong sensor ID type, no tests |
| **DeepSeek-V3.2** | Requesty | aborted (6m) | ~$0.52 | **8/40** | Aborted; only partial data layer, no service or controller |
| **GPT-OSS Safeguard 20B** | Local | 18m | free | **5/40** | Used `@Service`/`@Component`, needed constant prompting |
| **Qwen Turbo** | Requesty | 12m | ~$0.16 | **4/40** | Repeated tool call failures; code written to wrong directories, broke the build |

### Key Takeaways — Implementation

- **GPT 5.4 is the top scorer at 37/40** — the richest JPA model (`@MapsId`, `JOIN FETCH`, full `@OneToMany`) with full sensor details in 9 minutes. It bypasses `SensorService` via `EntityManager.find` but avoids accessing package-protected repositories, making it a middle-ground approach.
- **Sonnet and GLM tie at 35/40**. Sonnet is the only model that correctly used `SensorService` for cross-feature sensor validation — the architecturally cleanest implementation. GLM produced the richest API response via JPA graph traversal.
- **GPT 5.3 Codex, Codex High, and Codex Extra-High all score 34/40**, forming a tight cluster. Extra-High added more test depth but introduced a layering violation (`JpaSensorLookupRepository`). Codex and Codex High are cleaner at the same score.
- **GPT 5.3 Codex effort levels show diminishing returns**: Low (33/40, 9m), Default (34/40, 10m), High (34/40, 14m), Extra-High (34/40, 20m). Default effort is the best value for this codebase.
- **Opus plans well but implements at 32/40** — it caught a `DataIntegrityViolationException` as a proxy for sensor validation, bypassing the layering rule. Planning ability does not predict implementation quality.
- **Kimi was the fastest cloud model (6 min)** but shipped a critical `findAll().stream().filter()` performance bug that would break at scale.
- **GPT models generally avoided the DTO-in-service anti-pattern** that affected nearly all other models — a notable improvement in instruction-following for service layer boundaries.
- **Local models are insufficient for this workflow**: The best local implementation (Devstral Small 2, 13/40) only produced the data layer after 54 minutes. No local model produced a working feature under these conditions.

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
| Medium | Qwen Turbo | 58s | Fast but low quality (8/40) |
| Medium | Devstral Small 2 (local) | 1m 28s | Slow for a local model |
| Medium | MiniMax M2.5 | 1m 37s | Decent speed for the quality |
| Medium | Nemotron 3 Nano (local) | 1m 42s | Slow with mediocre output |
| Slow | Claude Sonnet 4.6 | 2m 23s | Worth the wait (35/40) |
| Slow | GPT-OSS Safeguard (local) | 2m 41s | Slowest for worst result (9/40) |
| Slow | GLM 4.7 | 2m 57s | Worth the wait (34/40) |
| Slow | Claude Opus 4.6 | 3m 8s | Slowest, but best plan (36/40) |

### Implementation Phase

| Speed | Model | Time | Notes |
|-------|-------|:----:|-------|
| Fast | Kimi K2.5 | 6m | Fastest cloud implementation |
| Fast | GPT 5.3 Codex Low | 9m | Best GPT value; full sensor details |
| Fast | GPT 5.4 | 9m | Top scorer at 37/40; fastest high-quality result |
| Medium | Claude Sonnet 4.6 | 10m | Best Anthropic implementation; correct layering |
| Medium | GPT 5.3 Codex | 10m | Default effort; 34/40 |
| Medium | Claude Opus 4.6 | 12m | Same time as Sonnet, lower score |
| Medium | GLM 4.7 | 12m | Ties Sonnet at 35/40 |
| Medium | MiniMax M2.5 | 12m | Decent for the low cost |
| Medium | Qwen Turbo | 12m | 12 minutes wasted (4/40) |
| Medium | Claude Haiku 4.5 | 13m | Needed a follow-up prompt |
| Medium | GPT 5.3 Codex High | 14m | Same score as default, 40% slower |
| Slow | GPT-OSS Safeguard (local) | 18m | Needed constant prompting |
| Slow | GPT 5.3 Codex Extra-High | 20m | Same score as default for double the time |
| Slow | Devstral | 28m | Too slow for the quality |
| Very slow | Devstral Small 2 (local) | 54m | Nearly an hour for an incomplete result |
| Very slow | Nemotron 3 Nano (local) | 54m | Nearly an hour, stopped on its own |

### Best time-to-quality ratio

For a complete plan + implementation cycle:

| Model | Total time | Combined score | Cost |
|-------|:----------:|:--------------:|:----:|
| Claude Sonnet 4.6 | ~12m | 70/80 | subscription |
| GLM 4.7 | ~15m | 69/80 | ~$2.71 |
| Kimi K2.5 | ~7m | 58/80 | ~$1.84 |
| Claude Haiku 4.5 | ~14m | 56/80 | subscription |

For implementation only (GPT models — planning evaluation pending):

| Model | Impl time | Impl score | Cost |
|-------|:---------:|:----------:|:----:|
| GPT 5.4 | 9m | 37/40 | subscription ($10/mo) |
| GPT 5.3 Codex Low | 9m | 33/40 | subscription ($20/mo) |
| GPT 5.3 Codex | 10m | 34/40 | subscription ($20/mo) |
| GPT 5.3 Codex Extra-High | 20m | 34/40 | subscription ($20/mo) |

---

## Cost Guidance

### Subscription vs. Pay-Per-Use: When Does Each Make Sense?

The key question for hobby coders: **is a GitHub Copilot subscription worth it, or should you pay per use via Requesty?**

#### GitHub Copilot Pro ($10/month)

GitHub Copilot Pro costs $10/month and includes **300 premium requests**.
Each request costs $10 / 300 = **\~$0.033**.
Haiku and Sonnet consume **1 request** per prompt; Opus consumes **3 requests**.
This evaluation used **2 prompts per feature** (plan + implementation).

| Workflow | Requests per feature | Cost per feature |
|----------|:--------------------:|:----------------:|
| Sonnet plan + Sonnet impl | 2 | ~$0.07 |
| Opus plan + Sonnet impl | 4 | ~$0.13 |
| Haiku plan + Haiku impl | 2 | ~$0.07 |

Additional requests beyond 300 are billed at **$0.04 each**.

#### Break-even: Subscription vs. Requesty

The best pay-per-use option by quality is **GLM 4.7** at ~$2.71/feature.
The cheapest viable option is **MiniMax M2.5** at ~$0.26/feature.

The break-even point is the number of features at which the $10 subscription fee is recovered compared to Requesty.
Below that number, pay-per-use is cheaper; above it, the subscription wins.

| Comparison | Copilot Pro cost/feature | Requesty cost/feature | Break-even |
|------------|:------------------------:|:---------------------:|:----------:|
| Sonnet vs GLM 4.7 | ~$0.07 | ~$2.71 | **~4 features** |
| Sonnet vs GLM plan + MiniMax impl | ~$0.07 | ~$0.49 | **~24 features** |
| Sonnet vs MiniMax M2.5 | ~$0.07 | ~$0.26 | **~53 features** |

**Bottom line**: If you implement more than ~4 features per month using GLM 4.7, a **Copilot Pro subscription is cheaper** — and delivers higher-quality results.
For most hobby coders doing any regular development, that threshold is trivial to cross.

#### When Pay-Per-Use Makes Sense

- You code infrequently (fewer than ~10 features per month)
- You want to avoid a recurring subscription
- You prefer a European provider (Requesty routes through Nebius for some models)
- You want to experiment with different models without commitment

#### GitHub Copilot Pro+ ($39/month)

Pro+ costs $39/month and includes 1,500 premium requests (~$0.026/request).
At 2 requests/feature with Sonnet, that covers up to 750 features/month.
**Only consider Pro+ if you heavily use Opus** (3 requests/prompt) or need access to additional models — Pro is sufficient for Sonnet-based workflows.

#### ChatGPT Plus ($20/month) — GPT 5.3 Codex

ChatGPT Plus costs $20/month and provides access to GPT 5.3 Codex at multiple effort levels. Unlike token-based or request-based pricing, ChatGPT Plus uses **daily and weekly usage caps** — each task consumes a percentage of your budget.

| Effort level | Daily cap/task | Tasks/day | Est. cost/task | Impl score |
|-------------|:--------------:|:---------:|:--------------:|:----------:|
| Low | 4% | ~25 | ~$0.02 | 33/40 |
| Default | 6% | ~16 | ~$0.04 | 34/40 |
| High | 8% | ~12 | ~$0.04 | 34/40 |
| Extra-High | 12% | ~8 | ~$0.08 | 34/40 |

**Key insight for hobby coders**: The daily cap is the practical constraint. At the default effort level, ~16 tasks per day is ample for hobby coding. The weekly cap (e.g., 2% = 50 tasks/week) only matters if you code intensively every day.

**Diminishing returns on effort**: Default, High, and Extra-High all scored 34/40. Low scored 33/40. Extra-High gained nothing over Default for double the cost and double the time. **The default effort level is the best value** — higher effort buys more thorough tests but not better architecture.

**ChatGPT Plus vs. GitHub Copilot Pro**: For pure implementation quality, GPT 5.4 on Copilot Pro ($10/mo, 37/40) significantly outperforms all Codex variants on ChatGPT Plus ($20/mo, 33-34/40). The only reason to prefer ChatGPT Plus is if you need its other features (web browsing, file upload, DALL-E, etc.) and want coding as an add-on. For dedicated coding, Copilot Pro with GPT 5.4 (37/40) or Sonnet 4.6 (35/40) is both cheaper and higher quality.

**Note**: GPT 5.3 Codex runs as an asynchronous background task in ChatGPT, not as an interactive agent session. This changes the workflow compared to OpenCode with Copilot or Requesty — you submit a task and wait for results rather than interacting in real time.

---

## Recommendations

Rather than looking for a single best model, the more useful question is: **which model is best for each role in your workflow?** Planning and implementation are different tasks — a model that writes excellent plans does not necessarily write the best code, and vice versa.

### Planning: Claude Opus 4.6 or Claude Sonnet 4.6 (GitHub Copilot)

Opus produces the best plans (36/40), with the strongest architectural reasoning, explicit cross-cutting concerns, and the most accurate codebase alignment. 
Sonnet follows closely (35/40) with the cleanest API design and most complete configuration examples.
With Opus consuming three times more credits, it is worth to stick with Sonnet for features with low or medium complexity.

For pay-per-use, **GLM 4.7** is the best cloud planner at ~$0.25 per plan (34/40), with detailed Java code examples and a rich test plan.

### Implementation: GPT 5.4 or Claude Sonnet 4.6 (GitHub Copilot)

**GPT 5.4** is the top implementation scorer at 37/40 — the richest JPA model, full sensor details, comprehensive tests, and completes in ~9 minutes on a flat subscription.

**Sonnet 4.6** scores 35/40 and is the only model that correctly used `SensorService` for cross-feature validation — the architecturally cleanest implementation. It completes in ~10 minutes on the same subscription.

**GPT 5.4** (37/40) is the best choice when raw implementation quality is the priority. **Sonnet 4.6** (35/40) is better when architectural correctness and layering compliance matter most.

For pay-per-use implementation, **GLM 4.7** (~$2.46, 35/40) ties Sonnet and is the only other model to return full sensor details in the API response.

### Recommended Setup

**Subscription (GitHub Copilot Pro — $10/month):**
Use **Opus 4.6** or **Sonnet 4.6** to produce the plan, review and adjust it, then use **GPT 5.4** or **Sonnet 4.6** to implement. 
You get 300 premium requests/month, enough for ~75-150 features depending on model choice.
**GPT 5.4** (37/40) is the top implementation scorer; **Sonnet 4.6** (35/40) is the better choice when architectural correctness matters most.

**ChatGPT Plus ($20/month):**
GPT 5.3 Codex scored 33-34/40 across all effort levels. At the default effort (~$0.04/task, 34/40), it's adequate but not competitive with GPT 5.4 (37/40) or Sonnet (35/40) on Copilot Pro at half the subscription cost. **ChatGPT Plus is not recommended as a primary coding subscription** — it makes sense only if you already subscribe for other ChatGPT features and want coding as a bonus.

**Pay-per-use (Requesty):**
Use **GLM 4.7** for planning (~$0.25) and implementation (~$2.46). 
Total cost per feature: ~$2.71. The quality is strong but the implementation cost is high — at this price, a Copilot Pro subscription pays for itself after just 4 features.

**Best value hybrid:**
Use **GLM 4.7** for planning (~$0.25 via Requesty) and **MiniMax M2.5** for implementation (~$0.24). Total ~$0.49 per feature — the lowest cost for a fully functional result.

### Local Models

No local model came close to matching cloud models. 
Devstral Small 2 scored highest locally (13/40) but only produced the data layer — no service, controller, or DTOs. 
Nemotron 3 Nano 30B (11/40) took 54 minutes and stopped without feedback. Gemma 3 12B failed entirely due to constant tool call failures. 
**Local models on a Mac M4 Mini 32 GB are insufficient for this kind of structured, multi-file Spring Boot feature development** — they lack the context handling, tool-calling reliability, and instruction-following needed to follow strict architectural conventions and multi-file testing patterns — even in a single-feature codebase. It is worth exploring whether smaller, well-scoped tasks or lighter context requirements would yield better results, but that is out of scope for this evaluation.

### Models to Avoid

- **GPT 5.3 Codex High/Extra-High effort levels**: No meaningful quality improvement over the default effort level (34/40 for all) at 1.5-2× the time and cost. Stick with default effort.
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
| `gh-<model>/device-feature` | GitHub Copilot model implementation |
| `oai-<model>/devices-feature` | ChatGPT Plus (OpenAI) model implementation |
| `rq-<model>/devices-feature` | Requesty model implementation |
| `<model>/devices-feature` | Local model implementation |

### Technologies

- **Java 21** with records, sealed classes, pattern matching
- **Spring Boot 3.x** with constructor injection and `@Configuration` beans
- **PostgreSQL 17** with Flyway migrations
- **Spring Data JPA** with keyset pagination
- **TestContainers** for repository integration tests
- **MockMvc** for controller tests
- **Springdoc OpenAPI** for API documentation
