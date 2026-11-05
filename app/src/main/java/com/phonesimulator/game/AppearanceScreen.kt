package com.phonesimulator.game

import android.widget.LinearLayout

internal object AppearanceScreen {
    fun render(host:MainActivity){
        val look=host.look
        host.screen("Оформление игры")
        host.label("Твой Phone Lab",24f,look.accentColor)
        host.label("Бесплатное оформление для текущего игрока. Цвета редкости телефонов сохраняют своё значение.",15f,look.muted)
        val preview=host.glassPanel()
        host.label("ПРЕДПРОСМОТР",12f,look.accentColor,preview)
        host.label("Коллекция в твоём стиле",21f,parent=preview)
        val model=host.gameState.owned.firstOrNull() ?: 24
        preview.addView(PhoneArtView(host,Catalog.phones[model]),LinearLayout.LayoutParams(-1,(120*host.resources.displayMetrics.density).toInt()))
        host.label(Catalog.phones[model].name,17f,parent=preview)
        host.label("Тема: ${AppLook.themes[look.theme]}",19f)
        host.button("Сменить тему"){host.applyLook(look.copy(theme=(look.theme+1)%4))}
        host.label("Акцент: ${AppLook.accentNames[look.accent]}",19f,look.accentColor)
        host.button("Сменить акцент"){host.applyLook(look.copy(accent=(look.accent+1)%6))}
        host.label("Фон: ${AppLook.backgrounds[look.background]}",19f)
        host.button("Сменить фон"){host.applyLook(look.copy(background=(look.background+1)%3))}
        host.label("Карточки: ${AppLook.cornerNames[look.corners]}",19f)
        host.button("Сменить форму карточек"){host.applyLook(look.copy(corners=(look.corners+1)%3))}
        host.label("Анимации: ${AppLook.motions[look.motion]}",19f)
        host.label("Минимальные: только короткое появление экранов. Выключенные системные анимации имеют приоритет.",14f,look.muted)
        host.button("Сменить анимации"){host.applyLook(look.copy(motion=(look.motion+1)%3))}
        host.label("Текст: ${if(look.largeText)"Увеличенный" else "Стандартный"}",19f)
        host.button("Изменить размер текста"){host.applyLook(look.copy(largeText=!look.largeText))}
        host.button("Сбросить оформление"){host.applyLook(AppLook())}
        host.button("На главную"){host.worldGo("home")}
    }
}
