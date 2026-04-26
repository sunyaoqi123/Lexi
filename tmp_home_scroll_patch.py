from pathlib import Path
p = Path(r"D:\Lexi\app\src\main\java\com\syq\lexi\ui\screens\HomeScreen.kt")
s = p.read_text(encoding="utf-8")

s = s.replace("import androidx.compose.foundation.lazy.LazyColumn\n", "")
s = s.replace("import androidx.compose.foundation.lazy.items\n", "")

s = s.replace(
"""            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)
                    .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(plans) { plan ->
                        val wb = wordbooks.find { it.id == plan.wordbookId }
                        if (wb != null) {
                            val total = wordCounts[wb.id] ?: wb.totalWords
                            val mastered = masteredCounts[wb.id] ?: 0
                            val days = if (plan.dailyWords > 0 && total > 0)
                                ceil(total.toFloat() / plan.dailyWords).toInt() else 0
                            PlanCard(name = wb.name, category = wb.category,
                                totalWords = total, masteredWords = mastered,
                                dailyWords = plan.dailyWords, daysNeeded = days,
                                onDelete = { onDeletePlan(plan) })
                        }
                    }
                }
            }""",
"""            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    plans.forEach { plan ->
                        val wb = wordbooks.find { it.id == plan.wordbookId }
                        if (wb != null) {
                            val total = wordCounts[wb.id] ?: wb.totalWords
                            val mastered = masteredCounts[wb.id] ?: 0
                            val days = if (plan.dailyWords > 0 && total > 0)
                                ceil(total.toFloat() / plan.dailyWords).toInt() else 0
                            PlanCard(name = wb.name, category = wb.category,
                                totalWords = total, masteredWords = mastered,
                                dailyWords = plan.dailyWords, daysNeeded = days,
                                onDelete = { onDeletePlan(plan) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }""",
1)

p.write_text(s, encoding="utf-8")
