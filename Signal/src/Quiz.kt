package signal

// Stable album numbers for models with recognisably different designs.
private val quizPool = listOf(1, 2, 4, 7, 8, 9, 10, 11, 18, 20, 31, 34, 58, 199, 251, 415, 416, 15722)
    .map { canonicalId(it - 1) }.distinct()
private const val quizLength = 5
private const val quizPrize = 250L

private class QuizQuestion(val phone: Int) {
    val choices = (quizPool.filter { it != phone }.shuffled().take(2) + phone).shuffled()
    var answer: Int? = null
    var credited = 0L
}

private fun newQuiz() = quizPool.shuffled().take(quizLength).map(::QuizQuestion)
private var questions = newQuiz()
private var questionIndex = 0

internal fun quizAction(action: String) {
    val question = questions.getOrNull(questionIndex)
    when {
        action == "quiz-replay" && question == null -> {
            questions = newQuiz()
            questionIndex = 0
        }
        action == "quiz-next" && question?.answer != null -> questionIndex++
        action.startsWith("quiz-answer-") && question != null && question.answer == null -> {
            val id = action.removePrefix("quiz-answer-").toIntOrNull() ?: return
            if (id !in question.choices) return
            question.answer = id
            if (id == question.phone) {
                question.credited = creditRubles(quizPrize)
                save()
            }
        }
        else -> return
    }
    render()
    document.querySelector(".quiz-panel h2")?.focus()
}

internal fun quizScreen(): String {
    val question = questions.getOrNull(questionIndex)
    val heading = title("Угадай телефон", "Выбери модель по фотографии. За верный ответ — ${rubles(quizPrize)}.")
    if (question == null) {
        val score = questions.count { it.answer == it.phone }
        val total = questions.sumOf { it.credited }
        return heading + """<section class="quiz-panel quiz-summary"><h2 tabindex="-1">Раунд пройден</h2><p>Верных ответов: $score из $quizLength</p><p>Заработано: ${rubles(total)}</p>${btn("quiz-replay", "Сыграть ещё")}</section>"""
    }
    val answered = question.answer != null
    val options = question.choices.joinToString("") { id ->
        val state = when {
            answered && id == question.phone -> " correct"
            id == question.answer -> " incorrect"
            else -> ""
        }
        """<button class="quiz-option$state" data-act="quiz-answer-$id" ${if (answered) "disabled" else ""}>${phones[id].name}</button>"""
    }
    val feedback = if (answered) {
        val model = phones[question.phone].name
        val message = if (question.answer == question.phone) "Верно! Это $model. +${rubles(question.credited)}" else "Это $model."
        """<p role="status">$message</p>""" + btn("quiz-next", if (questionIndex + 1 == quizLength) "Результат" else "Следующее фото")
    } else ""
    return heading + """<section class="quiz-panel"><div class="quiz-photo"><img src="${art("phone${question.phone}")}" alt="Телефон, модель которого нужно угадать"></div><div class="quiz-controls"><h2 tabindex="-1">Фото ${questionIndex + 1} из $quizLength</h2>$options$feedback</div></section>"""
}
