package com.phonesimulator.game

import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import kotlin.random.Random

internal fun <T> WorldScreens.pages(items: List<T>,parent: LinearLayout,draw: (T,LinearLayout)->Unit) {
    var shown=0;var more: Button?=null
    fun next(){more?.let { parent.removeView(it) };items.drop(shown).take(24).forEach { draw(it,parent) };shown=minOf(shown+24,items.size)
        more=if(shown<items.size)button("Ещё · $shown из ${items.size}",parent){next()} else null}
    next()
}
internal fun WorldScreens.inventory(){
    identify();begin("Экземпляры и мутации")
    text("У каждого экземпляра свои состояние, мутация и виртуальная ОС. Повреждённые телефоны можно восстановить до максимального состояния.",color=light)
    text("Варианты: Gold 8%, Diamond 4%, Galaxy 3%, Glitch 2%, Rainbow 1%. Остальные 82% — стандарт. Это независимый бросок от редкости модели.",15f,color=light)
    val search=input("Поиск экземпляров")
    val list=LinearLayout(host).apply { orientation=LinearLayout.VERTICAL;root.addView(this) }
    fun refresh(value: String){
        list.removeAllViews();val devices=p.devices().filter { (it.phone.name+" "+it.traits.mutation.title).contains(value.trim(),true) }
        if(devices.isEmpty())text("Коллекция пуста или ничего не найдено",parent=list)
        pages(devices,list){ item,parent ->
            deviceCard(item,parent)
            if(item.company==null && item.traits.condition<3){
                val cost=if(item.used)p.repairPrice(p.used.indexOfFirst { it.uid==item.traits.uid })
                    else p.repairOwnedPrice(p.owned.indices.first { p.traitsAt(it).uid==item.traits.uid })
                button("Ремонт · $cost ₽",parent){update(p.repairDevice(item.key),"Ремонт собственного телефона")}
            }
            button("Продать экземпляр · ${item.value} ₽",parent){
                AlertDialog.Builder(host).setTitle("Продать ${item.phone.name}?")
                    .setMessage("${item.traits.mutation.title}, ${Market.conditions[item.traits.condition]}. Будет продан только этот экземпляр.")
                    .setNegativeButton("Отмена",null).setPositiveButton("Продать"){_,_->update(p.sellDevice(item.key),"Продажа экземпляра")}.show()
            }
            button("На аукцион · ${item.phone.name}",parent){selectedKey=item.key;go("world_auctions")}
        }
    }
    search.addTextChangedListener(object:TextWatcher {override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int){};override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int){refresh(s.toString())};override fun afterTextChanged(s: Editable?){} })
    refresh("");back()
}
internal fun WorldScreens.exchange(){
    identify();begin("Биржа смартфонов")
    text("Внутриигровые котировки меняются каждые 30 минут. Это симуляция: покупки стоят 110% котировки, продажи — 90%, без реальных денег.",color=light)
    val now=System.currentTimeMillis();val slot=StockMarket.slot(now)
    text("До обновления: ${(StockMarket.INTERVAL-now%StockMarket.INTERVAL)/1000} с",15f,color=light)
    button("Обновить котировки"){go("world_exchange")}
    val search=input("Поиск на бирже",query)
    val list=LinearLayout(host).apply { orientation=LinearLayout.VERTICAL;root.addView(this) }
    fun refresh(value: String){
        list.removeAllViews();val matches=Catalog.phones.indices.filter { !Catalog.isUltra(it) && Catalog.phones[it].name.contains(value.trim(),true) }
        pages(matches,list){ model,parent ->
            val quote=StockMarket.quote(model,now);val previous=StockMarket.quote(model,(now-StockMarket.INTERVAL).coerceAtLeast(0))
            text(Catalog.phones[model].name,21f,parent)
            text("${if(quote>=previous) "↑" else "↓"} $quote ₽ · покупка ${StockMarket.buyPrice(model,now)} ₽",17f,parent)
            text("Последние 6 периодов: "+(5 downTo 0).joinToString(" → "){StockMarket.factor(model,(slot-it).coerceAtLeast(0)).toString()+"%"},13f,parent,light)
            button("Купить на бирже · ${Catalog.phones[model].name}",parent){update(p.buyStock(model,slot,System.currentTimeMillis()),"Покупка на бирже")}
        }
    }
    search.addTextChangedListener(object:TextWatcher {override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int){};override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int){query=s.toString();refresh(query)};override fun afterTextChanged(s: Editable?){} })
    refresh(query)
    text("Продать свои телефоны по котировке",23f)
    pages(p.devices(),root){ item,parent ->
        text("${item.phone.name} · ${item.traits.mutation.title}",18f,parent)
        button("Продать на бирже · ${StockMarket.sellPrice(item,now)} ₽",parent){update(p.sellStock(item.key,slot,System.currentTimeMillis()),"Продажа на бирже")}
    };back()
}
internal fun WorldScreens.auctions(){
    identify();begin("Аукцион")
    text("Покупатели виртуальные. Лот длится 2 минуты, ставки обновляются каждые 15 секунд. Комиссия 2% резерва (минимум 100 ₽) не возвращается. Если ставок нет, телефон возвращается после окончания.",color=light)
    chosen()?.let { item ->
        deviceCard(item,actions=false)
        for(percent in listOf(80,100,120)){
            val reserve=(item.value.toLong()*percent/100).toInt();val fee=(reserve/50).coerceAtLeast(100)
            button("Выставить: резерв $reserve ₽ · комиссия $fee ₽"){
                AlertDialog.Builder(host).setTitle("Выставить этот экземпляр?").setMessage("Телефон покинет инвентарь до завершения торгов. Комиссия: $fee ₽.")
                    .setNegativeButton("Отмена",null).setPositiveButton("Выставить"){_,_->if(update(p.listAuction(item.key,percent,System.currentTimeMillis()),"Комиссия аукциона"))selectedKey=""}.show()
            }
        }
    }
    if(p.world.auctions.isEmpty())text("Активных лотов нет. Выбери телефон в экземплярах.")
    p.world.auctions.forEach { lot ->
        val box=panel();text(lot.device.phone.name,22f,box);text("${lot.device.traits.mutation.title} · резерв ${lot.reserve} ₽",16f,box)
        val status=text("",17f,box)
        val claim=button("Завершить лот №${lot.id}",box){update(p.settleAuction(lot.id,System.currentTimeMillis()),"Результат аукциона")}
        fun refresh(){val now=System.currentTimeMillis();val bid=AuctionRules.bid(lot,now);val remaining=(lot.endsAt-now).coerceAtLeast(0)
            status.text="${if(bid>0) "${AuctionRules.bidder(lot,now)}: $bid ₽" else "Ставок пока нет"}\nОсталось ${(remaining+999)/1000} с"
            claim.isEnabled=remaining==0L}
        refresh();status.post(object:Runnable{override fun run(){if(!status.isAttachedToWindow)return;refresh();status.postDelayed(this,1000L)}})
    }
    button("Выбрать телефон для аукциона"){go("world_inventory")};back()
}
internal fun WorldScreens.albums(){
    begin("Коллекционные наборы")
    text("Собери указанные модели одновременно в коллекции. Мутации и состояние не мешают. Телефоны остаются у тебя; награда и жетон приложения выдаются один раз.",color=light)
    val models=(p.owned+p.used.map { it.phone }).toSet()
    Albums.all.forEach { album ->
        val box=panel();text(album.title,23f,box);val collected=album.models.count { it in models }
        text("$collected / ${album.models.size} · ${album.reward} ₽ + жетон",18f,box)
        album.models.forEach { text("${if(it in models) "✓" else "○"} ${Catalog.phones[it].name}",14f,box,light) }
        if(album.id in p.world.albums)text("Награда получена",parent=box)
        else button("Награда за набор · ${album.title}",box){update(p.claimAlbum(album.id),"Коллекционный набор")}.isEnabled=collected==album.models.size
    };back()
}
internal fun WorldScreens.company(){
    identify();begin("Своя компания")
    val c=p.world.company
    if(c.level==0){
        text("Создай бренд за 30 000 ₽. Выпускай игровые смартфоны, осматривай их в 3D, устанавливай приложения, продавай или выставляй на аукцион.",color=light)
        val name=input("Название бренда","Prism")
        button("Создать бренд · 30000 ₽"){update(p.createCompany(name.text.toString()),"Создание компании")}
    } else {
        text("${c.name} · уровень ${c.level}/5",25f);text("Выпущено: ${c.produced} · склад: ${c.phones.size}/20",18f)
        val series=input("Название серии",c.series)
        button("Сохранить название серии"){update(p.renameSeries(series.text.toString()),"Название серии")}
        if(c.level<5)button("Развить компанию · ${c.level*25000} ₽"){update(p.upgradeCompany(),"Развитие компании")}
        if(c.readyAt==0L){button("Выпустить смартфон · ${5000+c.level*1000} ₽"){update(p.startProduction(System.currentTimeMillis()),"Производство смартфона")}}
        else {
            val timer=text("")
            val collect=button("Забрать выпущенный смартфон"){update(p.collectProduction(System.currentTimeMillis()),"Готовая продукция")}
            fun refresh(){val remaining=(c.readyAt-System.currentTimeMillis()).coerceAtLeast(0);timer.text="Производство: ${(remaining+999)/1000} с";collect.isEnabled=remaining==0L}
            refresh();timer.post(object:Runnable{override fun run(){if(!timer.isAttachedToWindow)return;refresh();if(System.currentTimeMillis()<c.readyAt)timer.postDelayed(this,1000L)}})
        }
        text("Производство занимает 2 минуты. Параметры и цена продукции игровые, не характеристики реальных моделей.",15f,color=light)
        c.phones.forEach { phone ->deviceCard(DeviceSnapshot(-1,PhoneTraits(uid=phone.id),company=phone)) }
        button("Инвентарь и продажа"){go("world_inventory")}
    };back()
}
internal fun WorldScreens.season(){
    begin("Сезонное событие")
    val now=System.currentTimeMillis();val season=Seasons.current(now)
    text(season.title,27f);text("Сезон ${season.id}. До завершения ${(season.endsAt-now)/86400000} дн. Смена в полночь UTC первого числа.",17f,color=light)
    text("Ограниченный сезонный кейс: ${season.cost} ₽. В каждом открытии Палас 0,15%; Fold, Nokia 3310, Бурмалдофон и ТелИфон — по 0,1%. Остальные — из сезонной подборки.",18f)
    text("Первое открытие этого сезона навсегда разблокирует его оформление. Оно остаётся доступным в галерее после смены события.",17f,color=light)
    if(season.skin in p.world.seasonalSkins)text("Оформление этого сезона уже получено")
    button("Открыть сезонный кейс · ${season.cost} ₽"){
        val result=p.openSeasonal(season.id,Random.nextInt(10000),Random.nextInt(10000),Random.nextInt(10000),System.currentTimeMillis())
        if(result==null){toast("Недостаточно денег или сезон уже сменился");return@button}
        val identified=result.first.identifyPhones()
        if(!host.worldWrite(identified,"Сезонный кейс"))return@button
        drops=listOf(DeviceSnapshot(identified.owned.last(),identified.traitsAt(identified.owned.lastIndex)));revealed=false;go("world_batch")
    };back()
}
internal fun WorldScreens.batch(){
    begin(if(revealed) "Результат открытия ×${drops.size}" else "Открываем ×${drops.size}")
    if(!revealed && drops.isNotEmpty()){
        val reel=CaseOpeningView(host,drops.first().model,drops.size)
        root.addView(reel,LinearLayout.LayoutParams(-1,(250*host.resources.displayMetrics.density).toInt()))
        text("Оплата и телефоны уже сохранены. Все открытия независимы, гарантии Ultra нет.",color=light)
        button("Показать результат"){host.finishOpening()}
        back()
        host.playCaseOpening(reel){if(host.currentRoute=="world_batch" && !revealed){revealed=true;go("world_batch")}}
    } else {
        val best=drops.maxByOrNull { it.phone.rarity.ordinal }
        if(best!=null)root.addView(RewardBurstView(host,best.phone.rarity),LinearLayout.LayoutParams(-1,(64*host.resources.displayMetrics.density).toInt()))
        drops.forEachIndexed { index,item ->
            val before=root.childCount
            deviceCard(item,actions=p.device(item.key)!=null)
            if(index<10)GameMotion.enter(root.getChildAt(before),(index*45).toLong(),pop=true)
        }
        text("Результаты сохранены в коллекции. Мутации: ${drops.joinToString { it.traits.mutation.title }}",16f,color=light)
        button("Экземпляры"){go("world_inventory")};button("Кейсы"){go("cases")};back()
    }
}
