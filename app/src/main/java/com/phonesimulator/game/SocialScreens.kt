package com.phonesimulator.game

import kotlin.random.Random

private fun score(state: Progress): Long {
    val items=state.devices()+state.world.auctions.map { it.device }
    return items.sumOf { it.value.toLong()+it.phone.rarity.ordinal*3000L+it.traits.mutation.ordinal*2000L }+
        items.map { it.phone.name }.toSet().size*1000L
}
internal fun WorldScreens.ranking(){
    begin("Рейтинг коллекционеров")
    text("Офлайн-рейтинг: реальные локальные профили этого устройства и явно обозначенные виртуальные участники. Очки учитывают оценку телефонов, редкость, мутации и разные модели; деньги в кошельке не считаются.",16f,color=light)
    val book=LocalProfiles(host);val active=LocalProfiles.active(host)
    val locals=book.players().map { player ->
        val state=if(player.id==active)p else ProgressStore(host,player.id).load()
        "${player.name} · локальный игрок" to score(state)
    }
    val day=System.currentTimeMillis()/Progression.DAY_MS
    val bots=listOf("Неоновый Лис","Pixel-Архив","Золотой Мастер","Galaxy-Музей","Ретро-Клуб").mapIndexed { i,name ->
        "$name · виртуальный" to (50000L+(i+1)*(i+1)*45000L+day%31*(i+1)*500L)
    }
    (locals+bots).sortedByDescending { it.second }.forEachIndexed { i,row->text("${i+1}. ${row.first}\n${row.second} очков",20f) }
    text("Дуэли друзей на этом телефоне",23f)
    val logs=book.duels()
    book.players().forEach { player->text("${player.name}: ${logs.count { it.winner==player.id }} побед",18f) }
    text("Статистика по последним 200 локальным дуэлям. Виртуальные бои этого профиля: ${p.world.npcWins} побед / ${p.world.npcLosses} поражений.",15f,color=light)
    button("Игроки на этом телефоне"){go("world_players")};button("Битвы"){go("world_battles")};back()
}
internal fun WorldScreens.players(){
    begin("Локальные игроки")
    val book=LocalProfiles(host);val current=LocalProfiles.active(host)
    text("До четырёх отдельных сохранений. У каждого свои деньги, коллекция, компания и приложения. Передавай телефон другу и переключай профиль. Интернет не используется.",16f,color=light)
    book.players().forEach { player->
        val data=if(player.id==current)p else ProgressStore(host,player.id).load()
        val box=panel();text("${player.name}${if(player.id==current)" · сейчас играет" else ""}",22f,box)
        text("${data.coins} ₽ · ${data.owned.size+data.used.size+data.world.company.phones.size} телефонов",17f,box)
        if(player.id!=current)button("Играть: ${player.name}",box){host.switchPlayer(player.id)}
    }
    if(book.players().size<4){
        val name=input("Имя друга")
        button("Создать профиль друга"){
            if(book.create(name.text.toString())==null)toast("Имя от двух символов; максимум четыре профиля") else go("world_players")
        }
    }
    button("Дуэль с другом"){friendId="";duelPhase=0;go("world_duel")};back()
}
internal fun WorldScreens.battles(){
    identify();begin("Битвы смартфонов")
    text("Игровые рейтинги 1–999, а не реальные бенчмарки. Состояние и мутация влияют на очки. Тактики: стабильность сильнее разгона, энергосбережение сильнее стабильности, разгон сильнее энергосбережения.",16f,color=light)
    button("Дуэль с другом на одном телефоне"){friendId="";duelPhase=0;go("world_duel")}
    text("Против виртуального соперника",23f)
    text("Один бой в минуту. За победу 1000 ₽, за поражение деньги не снимаются.",16f,color=light)
    val now=System.currentTimeMillis();val opponent=Battles.opponent(now)
    val own=p.device(battleKey)
    if(own==null){text("Выбери свой телефон");pages(p.devices(),root){item,parent->
        button("В бой: ${item.phone.name} · ${item.traits.mutation.title}",parent){battleKey=item.key;go("world_battles")}
    }} else {
        text("${own.phone.name}\nпротив\n${opponent.phone.name}",22f)
        val a=Battles.stats(own);val b=Battles.stats(opponent)
        text("Игровая мощность: ${a.power} / ${b.power}\nИгровая камера: ${a.camera} / ${b.camera}\nИгровая автономность: ${a.battery} / ${b.battery}",18f)
        text("Чипы: ${own.phone.chip} / ${opponent.phone.chip}",15f,color=light)
        Battles.tactics.forEachIndexed { tactic,title->button("Тактика: $title"){
            val time=System.currentTimeMillis()
            if(Battles.opponent(time).model!=opponent.model){toast("Соперник сменился, обновляю");go("world_battles");return@button}
            val (next,result)=p.fightNpc(own.key,tactic,time,Random.nextInt(10000))
            npcResult=result
            if(next!=p)host.worldWrite(next,"Бой с виртуальным соперником")
            go("world_battles")
        }}
        button("Выбрать другого бойца"){battleKey="";go("world_battles")}
    }
    if(npcResult.isNotBlank())text(npcResult,20f)
    button("Рейтинг"){go("world_rank")};back()
}
internal fun WorldScreens.duel(){
    identify();begin("Дуэль на одном телефоне")
    val book=LocalProfiles(host);val current=LocalProfiles.active(host)
    val friends=book.players().filter { it.id!=current }
    val friend=friends.firstOrNull { it.id==friendId }
    if(friend==null){
        text("Выбери локальный профиль друга. У каждого должен быть хотя бы один телефон. В этой дуэли нет денежных ставок и наград.",16f,color=light)
        friends.forEach { player->button("Друг: ${player.name}"){friendId=player.id;friendKey="";battleKey="";duelPhase=0;duelResult="";go("world_duel")} }
        if(friends.isEmpty())text("Сначала создай профиль друга и соберите телефоны в обоих профилях.")
        button("Профили друзей"){go("world_players")};back();return
    }
    var friendState=ProgressStore(host,friend.id).load()
    val identified=friendState.identifyPhones()
    if(identified!=friendState && ProgressStore(host,friend.id).save(identified))friendState=identified
    val own=p.device(battleKey);val other=friendState.device(friendKey)
    when(duelPhase){
        0->{
            text("Ход 1: ${p.nickname}",24f)
            if(own==null){
                pages(p.devices(),root){item,parent->button("Первый игрок: ${item.phone.name} · ${item.traits.mutation.title}",parent){battleKey=item.key;go("world_duel")} }
                if(p.devices().isEmpty())text("У первого игрока пока нет телефонов")
            } else {
                text(own.phone.name,22f);val stats=Battles.stats(own);text("Мощность ${stats.power} · камера ${stats.camera} · автономность ${stats.battery}",17f)
                Battles.tactics.forEachIndexed { choice,title->button("Первый игрок: $title"){
                    if(duelPhase!=0)return@button
                    tacticA=choice;duelPhase=1;duelId=System.currentTimeMillis().coerceAtLeast(1);go("world_duel")
                }}
            }
        }
        1->{text("Передай телефон: ${friend.name}",25f);text("Выбор первого игрока скрыт. Второй игрок выбирает свой телефон и тактику.",17f,color=light)
            button("Второй игрок готов"){duelPhase=2;go("world_duel")}}
        2->{
            text("Ход 2: ${friend.name}",24f)
            if(other==null){
                pages(friendState.devices(),root){item,parent->button("Второй игрок: ${item.phone.name} · ${item.traits.mutation.title}",parent){friendKey=item.key;go("world_duel")} }
                if(friendState.devices().isEmpty())text("У друга пока нет телефонов. Вернитесь в профили и откройте кейс.")
            } else if(own==null){text("Первый телефон больше недоступен");button("Начать дуэль заново"){duelPhase=0;battleKey="";go("world_duel")}}
            else {
                text(other.phone.name,22f);val stats=Battles.stats(other);text("Мощность ${stats.power} · камера ${stats.camera} · автономность ${stats.battery}",17f)
                Battles.tactics.forEachIndexed { choice,title->button("Второй игрок: $title"){
                    if(duelPhase!=2)return@button
                    tacticB=choice
                    val a=Battles.score(own,tacticA,tacticB);val b=Battles.score(other,tacticB,tacticA)
                    val winner=if(a==b)"draw" else if(a>b)current else friend.id
                    if(!book.recordDuel(current,friend.id,winner,System.currentTimeMillis(),duelId)){toast("Не удалось сохранить результат");return@button}
                    val winnerName=if(winner=="draw") "Ничья" else "Победитель: "+(if(winner==current)p.nickname else friend.name)
                    duelResult="$winnerName\n${own.phone.name} / ${other.phone.name}\n${Battles.tactics[tacticA]} / ${Battles.tactics[tacticB]}\nОчки: $a / $b"
                    duelPhase=3;go("world_duel")
                }}
            }
        }
        else->{text(duelResult,23f);text("Результат сохранён в локальной таблице дуэлей. Коллекции и деньги обоих игроков не изменились.",16f,color=light)
            button("Ещё дуэль"){duelPhase=0;battleKey="";friendKey="";duelResult="";go("world_duel")};button("Таблица дуэлей"){go("world_rank")}}
    }
    button("Сменить соперника"){friendId="";duelPhase=0;go("world_duel")};back()
}
