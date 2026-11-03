package com.phonesimulator.game

data class RepairQuestion(val symptom: String, val choices: List<String>, val answer: Int)
data class RepairSession(val step: Int = 0, val mistakes: Int = 0) {
    init {
        require(step in 0..3 && mistakes in 0..3 && !(step == 3 && mistakes == 3))
    }
    val done: Boolean get() = step == 3 || mistakes == 3
    val bonus: Int get() = if (step == 3) listOf(500, 300, 100)[mistakes] else 0
    val stars: Int get() = if (step == 3) 3 - mistakes else 0
    fun answer(kind: ServiceKind, seed: Long, choice: Int): RepairSession {
        require(choice in 0..2)
        if (done) return this
        return if (choice == RepairGame.question(kind, step, seed).answer) copy(step = step + 1)
            else copy(mistakes = mistakes + 1)
    }
}

object RepairGame {
    // These are game situations, not instructions for servicing a real device.
    private val questions = mapOf(
        ServiceKind.REPAIR to listOf(
            "Телефон упал: экран чёрный, но звонки и вибрация работают. Что проверить?" to
                listOf("Дисплейный модуль", "Мелодию звонка", "Язык клавиатуры"),
            "В симуляторе нужно заменить дисплей. Как подготовить устройство?" to
                listOf("Выключить и обесточить", "Увеличить яркость", "Открыть игру"),
            "Дисплей заменён. Как проверить результат?" to
                listOf("Проверить изображение и касания", "Сразу убрать в коробку", "Поменять обои")
        ),
        ServiceKind.SETUP to listOf(
            "Клиент хочет перенести контакты. С чего начать?" to
                listOf("Проверить резервную копию", "Удалить старые контакты", "Выключить синхронизацию"),
            "Телефон не подключается к домашней сети. Что сверить?" to
                listOf("Имя сети и пароль", "Цвет чехла", "Размер шрифта"),
            "Настройка завершена. Что сделать перед выдачей?" to
                listOf("Проверить нужные клиенту функции", "Стереть все данные", "Удалить учётную запись")
        ),
        ServiceKind.ROM to listOf(
            "В игре клиент заказал кастомную систему. Как выбрать сборку?" to
                listOf("Для точной модели телефона", "Только по красивому названию", "Любую с большим номером"),
            "Перед изменением игровой системы что нужно сохранить?" to
                listOf("Резервную копию данных", "Только цвет обоев", "Ничего"),
            "Установка в симуляторе закончена. Что проверить?" to
                listOf("Запуск системы и основные функции", "Только логотип", "Только название прошивки")
        ),
        ServiceKind.JAILBREAK to listOf(
            "Клиент принёс iPhone или iPad. Что важно для игровой процедуры?" to
                listOf("Совместимость модели и версии iOS", "Цвет корпуса", "Марку зарядки"),
            "Что подготовить перед игровой модификацией?" to
                listOf("Резервную копию данных", "Новую мелодию", "Список обоев"),
            "Модификация в симуляторе завершена. Как принять работу?" to
                listOf("Проверить запуск и стабильность", "Сразу выдать без проверки", "Удалить резервную копию")
        )
    )
    fun question(kind: ServiceKind, step: Int, seed: Long): RepairQuestion {
        require(step in 0..2 && seed >= 0L)
        val (symptom, options) = questions.getValue(kind)[step]
        val shift = ((seed % 3 + step) % 3).toInt()
        return RepairQuestion(symptom, options.drop(shift) + options.take(shift), (3 - shift) % 3)
    }
}

/** Reject callbacks from old screens so double-tapping an answer cannot answer the next step. */
fun Progress.answerRepair(slot: Long, expected: RepairSession, choice: Int): Progress {
    require(choice in 0..2)
    val job = clientJob ?: return this
    if (job.offer.slot != slot || job.repair != expected || job.repair.done) return this
    return copy(clientJob = job.copy(repair = job.repair.answer(job.offer.kind, slot, choice)))
}
