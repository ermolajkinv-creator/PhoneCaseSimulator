package com.phonesimulator.game

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun WorldScreens.requirePhone(): DeviceSnapshot? {
    val phone=chosen()
    if(phone==null){begin("Телефон недоступен");text("Экземпляр продан или ещё не выбран.");button("Выбрать экземпляр"){go("world_inventory")};back()}
    return phone
}
private fun WorldScreens.requireApp(app: String): DeviceSnapshot? {
    val phone=requirePhone() ?: return null
    if(app !in p.virtualProfile(phone.key).apps){begin("Приложение заблокировано");button("Магазин приложений"){go("world_store")};button("Экран телефона"){go("world_phone")};return null}
    return phone
}
private fun appearance(item: DeviceSnapshot,profile: VirtualProfile): String {
    if(profile.theme!="default")return profile.theme
    if(item.phone.name=="Nokia 3310")return "mono"
    if(item.phone.system.startsWith("iPhone OS") || Regex("iOS [4-6]\\b").containsMatchIn(item.phone.system))return "ios"
    if(Regex("Android [2-4][. /]").containsMatchIn(item.phone.system))return "holo"
    return "modern"
}
internal fun WorldScreens.phoneHome(){
    val item=requirePhone() ?: return
    val profile=p.virtualProfile(item.key);val theme=appearance(item,profile)
    begin("Экран · ${item.phone.name}")
    val frame=LinearLayout(host).apply {
        orientation=LinearLayout.VERTICAL;setPadding(20,20,20,20)
        background=GradientDrawable(GradientDrawable.Orientation.TL_BR,when(theme){
            "mono","retro"->intArrayOf(0xFFA8B68B.toInt(),0xFF8C9D72.toInt())
            "pda"->intArrayOf(0xFF153D69.toInt(),0xFF277BA2.toInt())
            "ios"->intArrayOf(0xFFCFD5DE.toInt(),0xFF8996A8.toInt())
            "holo"->intArrayOf(0xFF10151D.toInt(),0xFF17374B.toInt())
            "glitch"->intArrayOf(0xFF00392C.toInt(),0xFF25143F.toInt())
            "galaxy"->intArrayOf(0xFF513680.toInt(),0xFF111936.toInt())
            "engineer"->intArrayOf(0xFF4F6666.toInt(),0xFF122C32.toInt())
            "rainbow"->intArrayOf(0xFF913B64.toInt(),0xFF3B407B.toInt(),0xFF246C61.toInt())
            else->intArrayOf(VirtualApps.colors[if(theme.startsWith("season:") || theme.startsWith("album:"))Math.floorMod(theme.hashCode(),6) else profile.wallpaper],0xFF131B38.toInt())
        }).apply { cornerRadius=32f }
        root.addView(this,LinearLayout.LayoutParams(-1,-2))
    }
    val mono=theme in listOf("mono","retro")
    val ink=if(mono)Color.BLACK else Color.WHITE
    val clock=SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())
    text("▰ 100%     $clock     ▂▄▆",15f,frame,ink).apply { if(mono)typeface=Typeface.MONOSPACE }
    text(when(theme){"mono","retro"->"NOKIA • МЕНЮ";"pda"->"▦ Пуск · Phone Mobile";"ios"->"Виртуальный iPhone OS";"holo"->"Виртуальный Android Classic";else->"Phone OS"},25f,frame,ink)
    if(theme=="galaxy" || theme.startsWith("season:") || theme.startsWith("album:") || theme=="engineer")text("✦ ${theme.replace("season:","Сезон ").replace("album:","Набор ")}",16f,frame,ink)
    text("${item.traits.mutation.title} · ${Market.conditions[item.traits.condition]}",15f,frame,ink)
    fun icon(title: String,action: ()->Unit){button(title,frame,action).apply {
        if(mono){setTextColor(Color.BLACK);typeface=Typeface.MONOSPACE;background=GradientDrawable().apply {setColor(0xFFB8C59A.toInt());setStroke(2,Color.BLACK);cornerRadius=0f}}
        if(theme=="ios")background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xFF647B96.toInt(),0xFF263C58.toInt())).apply {cornerRadius=18f;setStroke(1,Color.WHITE)}
    }}
    icon("⚙ Настройки"){go("world_settings")}
    profile.apps.forEach { app ->
        val route=when(app){"snake"->"world_snake";"clicker"->"world_clicker";"wallet"->"world_wallet";"messages"->"world_messages";"gallery"->"world_gallery";else->null}
        if(route!=null)icon(VirtualApps.titles[app].orEmpty()){go(route)}
    }
    if(profile.apps.isEmpty())text("Доступны базовые настройки. Новые приложения открываются в магазине внутри настроек.",16f,frame,ink)
    if(profile.animation=="pulse")frame.animate().alpha(0.65f).setDuration(250).withEndAction {frame.animate().alpha(1f).setDuration(250).start()}.start()
    if(profile.animation=="sparkle"){
        val stars=text("✦  ✧  ✦  ✧  ✦",24f,frame,ink)
        fun twinkle(){if(!stars.isAttachedToWindow)return;stars.animate().alpha(0.25f).setDuration(650).withEndAction {
            if(stars.isAttachedToWindow)stars.animate().alpha(1f).setDuration(650).withEndAction{twinkle()}.start() }.start()}
        stars.post{twinkle()}
    }
    button("3D-просмотр"){go("world_3d")};button("Инвентарь экземпляров"){go("world_inventory")};back()
}
internal fun WorldScreens.phoneSettings(){
    val item=requirePhone() ?: return
    begin("Настройки · ${item.phone.name}")
    text("Виртуальное устройство, не настройки настоящего телефона.",15f,color=light)
    text("Чип: ${item.phone.chip}\nДисплей: ${item.phone.display}\nПлатформа: ${item.phone.system}",17f)
    text("Мутация: ${item.traits.mutation.title}\nСостояние: ${Market.conditions[item.traits.condition]}\nЭкземпляр: ${item.key}",17f)
    button("Магазин приложений"){go("world_store")}
    button("Прослушать моно-рингтон"){playRingtone()}
    text("Один синтезированный голос, без загрузки файлов и без системных разрешений.",14f,color=light)
    if("gallery" in p.virtualProfile(item.key).apps)button("Оформление телефона"){go("world_gallery")}
    button("Прошивки и джейлбрейк в игре"){go("workshop")}
    button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.appStore(){
    val item=requirePhone() ?: return
    begin("Магазин приложений")
    text("Жетоны приложений: ${p.world.appTokens}. Один жетон заменяет цену одного приложения. Жетоны: 5% в обычном кейсе, награды достижений и наборов.",16f,color=light)
    VirtualApps.prices.forEach { (app,price) ->
        val box=panel();text(VirtualApps.titles.getValue(app),22f,box)
        if(app in p.virtualProfile(item.key).apps)text("Установлено на этот экземпляр",parent=box)
        else {
            button("Установить ${VirtualApps.titles.getValue(app)} · $price ₽",box){update(p.unlockApp(item.key,app,false),"Установка приложения")}
            if(p.world.appTokens>0)button("Открыть ${VirtualApps.titles.getValue(app)} за жетон",box){update(p.unlockApp(item.key,app,true),"Жетон приложения")}
        }
    };button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.gallery(){
    val item=requireApp("gallery") ?: return
    begin("Галерея и оформление")
    text("Настройки относятся к этому экземпляру. Изменение стоит 1000 игровых ₽; повторный выбор уже установленного варианта не списывает деньги.",16f,color=light)
    text("Цвет корпуса",22f)
    VirtualApps.colors.indices.forEach { color->button("Корпус ${color+1}"){update(p.customize(item.key,"color",color.toString()),"Цвет корпуса")} }
    text("Обои",22f)
    (0..5).forEach { wall->button("Обои ${wall+1}"){update(p.customize(item.key,"wallpaper",wall.toString()),"Обои")} }
    text("Темы и скины",22f)
    val names=mapOf("default" to "По умолчанию","pda" to "Классический КПК","retro" to "Пиксельный монохром","holo" to "Ранний Android","ios" to "Ранний iOS","galaxy" to "Галактика","glitch" to "Сбой","rainbow" to "Радуга","engineer" to "Мастер сервиса")
    (listOf("default","pda","retro","holo","ios","galaxy")+p.world.skins+p.world.seasonalSkins).distinct().forEach { theme ->
        button("Тема: ${names[theme] ?: theme}"){update(p.customize(item.key,"theme",theme),"Оформление телефона")}
    }
    text("Анимация",22f)
    listOf("none" to "Без анимации","pulse" to "Пульс","sparkle" to "Искры").forEach { (value,title)->button(title){update(p.customize(item.key,"animation",value),"Анимация телефона")} }
    button("Посмотреть в 3D"){go("world_3d")};button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.messages(){
    val item=requireApp("messages") ?: return
    begin("Сообщения · ${item.phone.name}")
    text("Вымышленные персонажи внутри офлайн-игры. Это не настоящая почта или переписка.",15f,color=light)
    listOf("Архивариус" to "Найди Nokia 3310 и сохрани её в коллекции. Код PRISM-RETRO откроет пиксельное оформление.",
        "Сбой 404" to "Улыбка скрывает секрет. Если у тебя есть Палас улыбашкэ, введи SMILE-404 для Glitch-скина.",
        "Мастер Лев" to "Выполни 3 заказа клиентов. Код MAST3R даст 5000 ₽ и оформление мастера.").forEach { (sender,message)->val box=panel();text(sender,22f,box);text(message,17f,box) }
    val code=input("Секретный код")
    button("Активировать код"){update(p.redeemCode(code.text.toString()),"Секретный код")}
    text("Каждый код сработает один раз на профиль, после выполнения условия.",15f,color=light)
    button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.wallet(){
    val item=requireApp("wallet") ?: return
    begin("Крипто-кошелёк · игра")
    text("Только игровые ₽. Никакой настоящей криптовалюты, платежей или вывода средств. Доход начисляется только пока приложение открыто на экране устройства.",16f,color=light)
    val active=p.device(p.world.miner.key)
    text("Работает: ${active?.phone?.name ?: "не выбрано"}",19f)
    text("Один генератор на профиль, максимум 30 минут накопленного активного времени.",16f,color=light)
    if(p.world.miner.key!=item.key)button("Активировать этот кошелёк"){update(p.activateMiner(item.key),"Активация кошелька")}
    val amount=text("",22f)
    fun refresh(){val current=p;amount.text="Ставка: ${current.minerRate()} ₽/мин\nНакоплено: ${current.world.miner.activeMs/60000*current.minerRate()} ₽\nАктивное время: ${current.world.miner.activeMs/1000} с"}
    refresh();amount.post(object:Runnable{override fun run(){if(!amount.isAttachedToWindow)return;refresh();amount.postDelayed(this,1000L)}})
    button("Забрать доход кошелька"){update(p.claimMining(),"Доход виртуального кошелька")}
    button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.phoneClicker(){
    val item=requireApp("clicker") ?: return
    begin("Кликер · ${item.phone.name}")
    text("Награда идёт в общий баланс профиля. Общий лимит с основным кликером: 12 нажатий/с, затем пауза 30 с.",16f,color=light)
    val status=text("");lateinit var coin: Button
    fun refresh(){val remaining=p.clickGuard.remaining(System.currentTimeMillis());coin.isEnabled=remaining==0L
        status.text=if(remaining>0)"Пауза: ${(remaining+999)/1000} с" else "Баланс ${p.coins} ₽ · кликов ${p.clicks}"}
    coin=button("◉ В телефоне +10 ₽"){
        val now=System.currentTimeMillis();val next=p.clickCoin(now/Progression.DAY_MS,now)
        if(next!=p)host.worldWrite(next,"Кликер виртуального телефона")
        refresh()
    }
    refresh();status.post(object:Runnable{override fun run(){if(!status.isAttachedToWindow)return;refresh();status.postDelayed(this,250L)}})
    button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.snakeGame(){
    val item=requireApp("snake") ?: return
    begin("Змейка · ${item.phone.name}")
    text("Пиксельное монохромное поле, 20×20. Собирай еду и не врезайся в стены или себя. Рекорд сохраняется; денежных наград здесь нет.",16f,color=light)
    val status=text("").apply {typeface=Typeface.MONOSPACE}
    lateinit var board: SnakeView
    fun refresh(state: SnakeState){status.text="Счёт ${state.score} · рекорд ${p.virtualProfile(item.key).bestSnake}"+(if(state.over)" · Игра окончена" else if(!board.running)" · Пауза" else "")}
    board=SnakeView(host,item.traits.uid){ state ->
        val profile=p.virtualProfile(item.key)
        if(state.score>profile.bestSnake && p.device(item.key)!=null)host.worldWrite(p.updateVirtualProfile(item.key,profile.copy(bestSnake=state.score)),"Рекорд Змейки")
        refresh(state)
    }
    if(snakeSavedKey==item.key && snakeSnapshot.isNotBlank())board.restore(snakeSnapshot)
    snake=board;snakeKey=item.key
    root.addView(board,LinearLayout.LayoutParams(-1,(320*host.resources.displayMetrics.density).toInt()))
    refresh(board.state)
    button("Начать / пауза"){board.playPause();refresh(board.state)}
    listOf("↑" to 0,"→" to 1,"↓" to 2,"←" to 3).forEach { (label,direction)->button(label){board.turn(direction)} }
    button("Экран телефона"){go("world_phone")};back()
}
internal fun WorldScreens.preview(){
    val item=requirePhone() ?: return
    begin("3D · ${item.phone.name}")
    text("Процедурная стилизованная 3D-модель: тяни пальцем для вращения на 360°, двумя пальцами меняй масштаб. Это не фотограмметрия точного корпуса.",16f,color=light)
    val viewer=Phone3DView(host,item,p.virtualProfile(item.key)).apply {tag="phone3d"}
    root.addView(viewer,LinearLayout.LayoutParams(-1,(380*host.resources.displayMetrics.density).toInt()))
    button("Повернуть на 90°"){viewer.rotate()};button("Приблизить"){viewer.zoomBy(1.2f)};button("Отдалить"){viewer.zoomBy(0.8f)};button("Сбросить камеру"){viewer.reset()}
    button("Экран телефона"){go("world_phone")};back()
}
