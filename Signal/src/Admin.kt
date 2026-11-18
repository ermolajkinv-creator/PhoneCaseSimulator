package signal

private const val ownerDigest = "5559762f09dbdefc1e37f3f3dd3cebf230724882455c071e7c59c6a7cdfcafc7"
var adminIsOpen = false
    private set
private var unlocked = false

fun leaveAdmin() {
    adminIsOpen = false
    unlocked = false
}

private fun status(id: String, text: String) {
    document.getElementById(id)?.textContent = text
}

private fun checkPassword() {
    val field = document.getElementById("owner-password") ?: return
    val password = field.value as String
    val crypto = window.crypto?.subtle
    if (crypto == null) {
        status("password-result", "Открой игру из скачанного файла, по HTTPS или в Android-приложении.")
        return
    }
    crypto.digest("SHA-256", password.encodeToByteArray()).then({ buffer: dynamic ->
        val bytes: ByteArray = js("new Int8Array(buffer)")
        val digest = bytes.joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
        if (field.isConnected == true && field.value == password) {
            if (digest == ownerDigest) {
                unlocked = true
                render()
                document.getElementById("admin-amount").focus()
            } else {
                status("password-result", "Неверный пароль.")
                field.select()
            }
        }
    }).catch({ _: dynamic -> status("password-result", "Не удалось проверить пароль. Попробуй ещё раз.") })
}

private fun modelOptions(query: String): String = matchingPhones(query).joinToString("") { id ->
    val phone = phones[id]
    val mark = if (id in owned) " · в альбоме" else ""
    """<option value="$id">${phone.name} · ${phone.year}$mark</option>"""
}

fun updateAdminModels() {
    if (!unlocked) return
    val options = modelOptions(document.getElementById("admin-search").value as String)
    document.getElementById("admin-model").innerHTML = options
    document.getElementById("issue-phone").disabled = options.isEmpty()
    status("phone-result", if (options.isEmpty()) "Модели не найдены." else "")
}

fun settings(): String {
    if (!adminIsOpen) return title("Настройки", "") +
        """<button class="settings-entry" data-act="admin-open"><span>Админ-панель<small>Монеты и коллекция</small></span><span aria-hidden="true">→</span></button>""" +
        btn("settings-memory", "???", "settings-entry mystery-entry")
    val heading = btn("admin-back", "← Настройки", "back-link") + title("Админ-панель", "")
    if (!unlocked) return heading + """
        <form class="side settings-form" id="admin-unlock">
          <label for="owner-password">Пароль администратора</label>
          <input id="owner-password" type="password" required autocomplete="current-password" autocapitalize="none" spellcheck="false" aria-describedby="password-result">
          <button type="submit" class="action">Войти</button>
          <p id="password-result" class="hint" role="status"></p>
        </form>"""
    return heading + """
        <div class="admin-grid">
          <form class="side settings-form" id="admin-credit">
            <h2>Монеты</h2>
            <label for="admin-amount">Сколько начислить</label>
            <input id="admin-amount" type="text" inputmode="numeric" pattern="[0-9]+" maxlength="10" value="1000" required>
            <button type="submit" class="action">Начислить монеты</button>
            <p id="credit-result" class="hint" role="status"></p>
          </form>
          <form class="side settings-form" id="admin-grant">
            <h2>Выдать телефон</h2>
            <label for="admin-search">Поиск по названию или году</label>
            <input type="search" id="admin-search" placeholder="Например, Fold8">
            <label for="admin-model">Модель</label>
            <select id="admin-model">${modelOptions("")}</select>
            <button id="issue-phone" type="submit" class="action">Добавить в альбом</button>
            <p id="phone-result" class="hint" role="status"></p>
          </form>
        </div>"""
}

fun adminAction(action: String) {
    when (action) {
        "admin-open" -> {
            adminIsOpen = true
            render()
            document.getElementById("owner-password").focus()
        }
        "admin-back" -> navigate(3)
        "admin-unlock" -> if (adminIsOpen && !unlocked) checkPassword()
        "admin-credit" -> if (unlocked) {
            val value = document.getElementById("admin-amount").value as String
            val amount = value.toIntOrNull()
            if (amount == null || amount <= 0 || amount > Int.MAX_VALUE - coins) {
                val room = Int.MAX_VALUE - coins
                val message = if (room == 0) "Баланс уже максимальный." else "Введи целую сумму от 1 до $room."
                status("credit-result", message)
                return
            }
            creditCoins(amount)
            save()
            val wallet = document.querySelector(".wallet")
            wallet.innerHTML = coin() + coins.toString()
            wallet.setAttribute("aria-label", "Баланс: $coins монет")
            status("credit-result", "Начислено $amount. Баланс — $coins." + saveNotice())
        }
        "admin-grant" -> if (unlocked) {
            val model = document.getElementById("admin-model")
            val id = (model.value as String).toIntOrNull() ?: return
            if (id !in phones.indices) return
            if (owned.add(id)) {
                save()
                updateAdminModels()
                model.value = id.toString()
                status("phone-result", "${phones[id].name} добавлен в альбом." + saveNotice())
            } else status("phone-result", "Этот телефон уже в альбоме.")
        }
    }
}

private fun saveNotice() = if (storageWarning) " Хранилище недоступно. Изменение действует до закрытия игры." else ""
