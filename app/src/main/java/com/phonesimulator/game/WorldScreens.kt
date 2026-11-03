package com.phonesimulator.game

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import kotlin.random.Random

internal class WorldScreens(val host: MainActivity) {
    val p: Progress get()=host.gameState
    val root: LinearLayout get()=host.worldRoot
    var selectedKey=""
    var query=""
    var selectedCase=CaseKind.CLASSIC
    var drops=emptyList<DeviceSnapshot>()
    var revealed=true
    var snake: SnakeView?=null
    var snakeKey=""
    var snakeSnapshot=""
    var snakeSavedKey=""
    private val ringtone=RingtonePreview()
    var battleKey=""
    var friendId=""
    var friendKey=""
    var duelPhase=0
    var tacticA=0
    var tacticB=0
    var duelId=0L
    var duelResult=""
    var npcResult=""
    val light get()=host.look.muted
    fun text(value: String,size: Float=17f,parent: LinearLayout=root,color: Int=Color.WHITE)=host.label(value,size,color,parent)
    fun button(value: String,parent: LinearLayout=root,action: ()->Unit)=host.button(value,parent,action)
    fun panel()=host.glassPanel()
    fun begin(title: String){host.screen(title)}
    fun go(route: String)=host.worldGo(route)
    fun back(){button("Мир телефонов"){go("world_hub")};button("Главное меню"){go("home")}}
    fun toast(value: String){Toast.makeText(host,value,Toast.LENGTH_SHORT).show()}
    fun input(hint: String,value: String="",parent: LinearLayout=root,numeric: Boolean=false): EditText = EditText(host).apply {
        this.hint=hint;setText(value);setTextColor(Color.WHITE);setHintTextColor(light);isSingleLine=true
        if(numeric)inputType=InputType.TYPE_CLASS_NUMBER
        filters=arrayOf(android.text.InputFilter.LengthFilter(60));parent.addView(this,LinearLayout.LayoutParams(-1,-2))
    }
    fun update(next: Progress,reason: String,redraw: Boolean=true): Boolean {
        if(next==p){toast("Действие недоступно: проверь баланс, место в кошельке, условия или таймер");return false}
        if(!host.worldWrite(next,reason))return false
        if(redraw)go(host.currentRoute)
        return true
    }
    fun identify(){val next=p.identifyPhones();if(next!=p)host.worldWrite(next,"Идентификаторы экземпляров")}
    fun chosen(): DeviceSnapshot?=p.device(selectedKey)
    fun openModel(model: Int,preview: Boolean=false) {
        identify();val item=p.devices().firstOrNull { it.model==model }
        if(item==null){toast("Сначала получи этот телефон в коллекцию");return}
        selectedKey=item.key;go(if(preview)"world_3d" else "world_phone")
    }
    fun openKey(key: String,preview: Boolean=false){
        if(p.device(key)==null){toast("Экземпляр недоступен");return}
        selectedKey=key;go(if(preview)"world_3d" else "world_phone")
    }
    fun openBatch(kind: CaseKind,count: Int) {
        val result=p.openMany(kind,count,List(count){Random.nextInt(10000)},List(count){Random.nextInt(10000)},
            List(count){Random.nextInt(10000)},System.currentTimeMillis()/Progression.DAY_MS,
            List(count){Random.nextInt(10000)})
        if(result==null){toast("Недостаточно денег для всех $count кейсов");return}
        val identified=result.first.identifyPhones()
        if(!host.worldWrite(identified,"Мультиоткрытие ×$count"))return
        drops=(identified.owned.size-count until identified.owned.size).map { DeviceSnapshot(identified.owned[it],identified.traitsAt(it)) }
        revealed=false;go("world_batch")
    }
    fun beforeNavigate(){
        snake?.let { if(it.running)it.playPause();snakeSnapshot=it.snapshot();snakeSavedKey=snakeKey }
        snake=null;stopSound()
    }
    fun stopSound(){ringtone.stop()}
    fun playRingtone(tune: Int=0){ringtone.play(tune)}
    fun saveState(bundle: Bundle){
        snake?.let { snakeSnapshot=it.snapshot();snakeSavedKey=snakeKey };stopSound()
        bundle.putString("worldKey",selectedKey);bundle.putString("worldQuery",query)
        bundle.putString("snakeState",snakeSnapshot);bundle.putString("snakeKey",snakeSavedKey)
        bundle.putBoolean("worldRevealed",revealed);bundle.putIntArray("worldDrops",drops.map { it.model }.toIntArray())
        bundle.putStringArray("worldMutations",drops.map { it.traits.mutation.name }.toTypedArray())
        bundle.putIntArray("worldConditions",drops.map { it.traits.condition }.toIntArray())
        bundle.putLongArray("worldUids",drops.map { it.traits.uid }.toLongArray())
        bundle.putString("worldNpcResult",npcResult)
        // Hot-seat selections are intentionally abandoned on recreation before a result.
    }
    fun restoreState(bundle: Bundle?){
        if(bundle==null)return
        selectedKey=bundle.getString("worldKey").orEmpty();query=bundle.getString("worldQuery").orEmpty()
        snakeSnapshot=bundle.getString("snakeState").orEmpty();snakeSavedKey=bundle.getString("snakeKey").orEmpty()
        revealed=bundle.getBoolean("worldRevealed",true);npcResult=bundle.getString("worldNpcResult").orEmpty()
        val models=bundle.getIntArray("worldDrops") ?: intArrayOf();val mutations=bundle.getStringArray("worldMutations") ?: emptyArray()
        val conditions=bundle.getIntArray("worldConditions") ?: intArrayOf();val uids=bundle.getLongArray("worldUids") ?: longArrayOf()
        drops=models.take(10).mapIndexedNotNull { i,model -> if(model !in Catalog.phones.indices)null else DeviceSnapshot(model,
            PhoneTraits(runCatching { Mutation.valueOf(mutations.getOrNull(i).orEmpty()) }.getOrDefault(Mutation.NORMAL),
                (conditions.getOrNull(i) ?: 3).coerceIn(0,3),(uids.getOrNull(i) ?: 0L).coerceAtLeast(0))) }
    }
    fun render(route: String){
        when(route){
            "world_hub"->hub();"world_inventory"->inventory();"world_exchange"->exchange();"world_auctions"->auctions()
            "world_albums"->albums();"world_company"->company();"world_season"->season();"world_batch"->batch()
            "world_phone"->phoneHome();"world_settings"->phoneSettings();"world_store"->appStore();"world_gallery"->gallery()
            "world_messages"->messages();"world_wallet"->wallet();"world_clicker"->phoneClicker();"world_snake"->snakeGame()
            "world_3d"->preview();"world_rank"->ranking();"world_players"->players();"world_battles"->battles()
            "world_duel"->duel();else->hub()
        }
    }
    fun hub(){
        begin("Мир телефонов")
        text("Новые возможности для коллекции. Все деньги виртуальные; игра работает офлайн.",parent=root,color=light)
        listOf("📱 Экземпляры и виртуальная ОС" to "world_inventory","🏆 Рейтинг коллекционеров" to "world_rank",
            "📈 Биржа смартфонов" to "world_exchange","🛠 Ремонт и мутации" to "world_inventory",
            "💰 Аукцион с виртуальными покупателями" to "world_auctions","🧩 Коллекционные наборы" to "world_albums",
            "⚔ Битвы и дуэли с другом" to "world_battles","🏢 Моя компания" to "world_company",
            "📅 Сезонное событие" to "world_season","👥 Игроки на этом телефоне" to "world_players").forEach { (title,page)->button(title){go(page)} }
        button("Кейсы и мультиоткрытие"){go("cases")}
        button("Мини-игра ремонта"){go("repair")}
        button("Главное меню"){go("home")}
    }
    fun deviceCard(item: DeviceSnapshot,parent: LinearLayout=root,actions: Boolean=true){
        val box=LinearLayout(host).apply { orientation=LinearLayout.VERTICAL;setPadding(16,12,16,12)
            background=host.themedPanel(if(item.traits.mutation==Mutation.NORMAL)item.phone.rarity.accent else item.traits.mutation.accent);parent.addView(this) }
        text(item.phone.name,22f,box);text("${item.phone.rarity.title} · ${item.traits.mutation.title} · ${Market.conditions[item.traits.condition]}",16f,box,item.traits.mutation.accent)
        text("Оценка: ${item.value} ₽",18f,box)
        val color=p.virtualProfile(item.key).color.let { if(it!=0)it else if(item.traits.mutation!=Mutation.NORMAL)item.traits.mutation.accent else item.company?.color ?: 0 }
        box.addView(PhoneArtView(host,item.phone,color),LinearLayout.LayoutParams(-1,host.resources.displayMetrics.density.times(150).toInt()))
        if(actions){
            button("Открыть экран · ${item.phone.name}",box){selectedKey=item.key;go("world_phone")}
            button("3D · ${item.phone.name}",box){selectedKey=item.key;go("world_3d")}
        }
    }
}
