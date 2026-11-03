package com.phonesimulator.game

/** Version 4.5 workshop campaign. Small immutable state, independent in each local profile. */
data class Empire(val values: Map<String,Long> = emptyMap(),val journal: List<String> = emptyList()) {
    operator fun get(key: String)=values[key] ?: 0L
}
data class EmpireResult(val progress: Progress,val message: String)
enum class EmpireFeature(val title: String,val description: String) {
    ROOM("Помещение", "Стол → гараж → мастерская → салон → сеть. Каждый уровень добавляет 300 ₽ к заказу. Требуется 3 заказа на предыдущий уровень."),
    TOOLS("Инструменты", "Пять уровней инструментов: +250 ₽ за каждый уровень при ремонте."),
    BENCH("Рабочий стенд", "Улучшенный стенд даёт +150 ₽ за ремонт и ускоряет поставки на 5 секунд за уровень."),
    DIAGNOSIS("Диагностика", "Прими заказ, прочитай симптомы и найди неисправность. Неверная диагностика стоит 200 ₽; гарантийный талон покрывает ошибку."),
    SCREEN("Замена экрана", "Заказ на разбитый экран: нужна диагностика и одна запчасть. Телефон клиента не забирается в коллекцию."),
    BATTERY("Замена батареи", "Заказ на изношенную батарею: нужна диагностика и одна запчасть."),
    CHARGE("Ремонт разъёма", "Заказ на неработающий USB: диагностика, две запчасти и точная пайка."),
    FLASH("Восстановление системы", "Заказ на зависание при загрузке: диагностика и виртуальная переустановка системы. Запчасти не нужны."),
    CLEAN("Чистка телефона", "Заказ на тихий динамик: диагностика и чистка сетки. Запчасти не нужны."),
    QUALITY("Контроль качества", "Сертификация проверок повышает оплату каждого ремонта на 200 ₽ за уровень."),
    PARTS("Склад запчастей", "Покупай комплекты по 400 ₽ за деталь. Ёмкость склада — 100 деталей."),
    RECYCLE("Разборка донора", "Разбери выбранный обычный экземпляр из коллекции на три детали. Телефон будет израсходован; ультралегендарные и мутировавшие защищены."),
    PARCEL("Неизвестная посылка", "За 6000 ₽ получи повреждённую бюджетную модель стоимостью до 5000 ₽. Её можно восстановить в мастерской экземпляров."),
    DELIVERY("Поставки", "Закажи 10 деталей за 3000 ₽ и дождись курьера. Заказ сохраняется при закрытии игры."),
    SUPPLIER("Поставщик", "Три уровня сотрудничества снижают цену будущей поставки на 200 ₽ за уровень."),
    STAFF("Помощник", "Найми помощника за 15000 ₽. Поручение стоит 500 ₽ и через минуту приносит 1200 ₽ плюс бонус обучения. Только одно поручение одновременно."),
    TRAINING("Обучение помощника", "Каждый из пяти курсов повышает выручку завершённого поручения на 200 ₽. Сумма фиксируется при старте."),
    RESEARCH("Исследования", "Три технологии снижают расход деталей в ремонте разъёма и экрана, затем увеличивают награду за системный ремонт."),
    ADS("Реклама салона", "Кампания за 1000 ₽ приносит две единицы репутации. Перезарядка — 10 минут."),
    WARRANTY("Гарантийные талоны", "Три талона за 500 ₽ защищают от списания денег при ошибочной диагностике. Максимум 30 талонов."),
    DAILY("Сменное задание", "Выполни три новых заказа за смену и забери 3000 ₽. Новая смена доступна раз в сутки UTC; награда выдаётся один раз."),
    STORY("История мастерской", "Восемь глав: выполняй заказы, развивай помещение и получай разовые награды."),
    SHOWCASE("Витрина салона", "Выбери принадлежащий тебе телефон для витрины. Витрина отображается в помещении и сохраняется отдельно для каждого игрока."),
    EXHIBITION("Выставка коллекционеров", "Пять разных моделей и пять единиц репутации дают право на ежедневную выставку: 2000 ₽ и одну репутацию."),
    JOURNAL("Журнал мастерской", "Последние 40 операций: расходы, ремонт, поставки, награды и развитие. Записи появляются только после успешного действия.")
}
object EmpireRules {
    val symptoms=listOf("Стекло разбито, изображение в полосах", "Заряд падает со 100% до 0% за минуту", "Кабель исправен, USB-разъём шатается", "Телефон застрял на логотипе", "Динамик тихий, сетка забита пылью")
    val faults=listOf("Экран", "Батарея", "Разъём", "Система", "Загрязнение")
    fun act(p: Progress,feature: EmpireFeature,choice: Int=0,now: Long=System.currentTimeMillis()): EmpireResult {
        val e=p.world.empire;val v=e.values.toMutableMap();var next=p;var delta=0L;var message=""
        fun get(k:String)=v[k] ?: 0L
        fun set(k:String,value:Long){v[k]=value}
        fun add(k:String,value:Long){set(k,(get(k)+value).coerceIn(0,1_000_000_000L))}
        fun reject(reason:String)=EmpireResult(p,reason)
        fun upgrade(key:String,max:Int,price:Long): Boolean {
            if(get(key)>=max)return false
            delta=-price*(get(key)+1);add(key,1);message="Улучшение: ${feature.title}, уровень ${get(key)}";return true
        }
        when(feature){
            EmpireFeature.ROOM->{if(get("jobs")<get("room")*3)return reject("Нужно ${get("room")*3} выполненных заказов");if(!upgrade("room",5,5000))return reject("Помещение максимального уровня")}
            EmpireFeature.TOOLS->if(!upgrade("tools",5,2000))return reject("Инструменты максимального уровня")
            EmpireFeature.BENCH->if(!upgrade("bench",5,2500))return reject("Стенд максимального уровня")
            EmpireFeature.QUALITY->if(!upgrade("quality",5,3000))return reject("Все сертификаты получены")
            EmpireFeature.SUPPLIER->if(!upgrade("supplier",3,4000))return reject("Максимальный уровень поставщика")
            EmpireFeature.TRAINING->{if(get("staff")==0L)return reject("Сначала найми помощника");if(!upgrade("training",5,3000))return reject("Все курсы пройдены")}
            EmpireFeature.RESEARCH->if(!upgrade("research",3,6000))return reject("Все технологии изучены")
            EmpireFeature.DIAGNOSIS->{
                if(get("job")==0L){set("job",get("accepted")%5+1);add("accepted",1);set("diagnosed",0);message="Принят заказ: ${symptoms[(get("job")-1).toInt()]}"}
                else if(get("diagnosed")==1L)return reject("Диагноз уже подтверждён")
                else if(choice==get("job").toInt()){set("diagnosed",1);message="Диагноз подтверждён: ${faults[choice-1]}"}
                else {if(choice !in 1..5)return reject("Выбери неисправность по симптомам");if(get("diagnosed")==1L)return reject("Диагноз уже подтверждён")
                    if(get("warranty")>0)add("warranty",-1) else delta=-200;message="Неверный диагноз. Проверь симптомы ещё раз"}
            }
            EmpireFeature.SCREEN,EmpireFeature.BATTERY,EmpireFeature.CHARGE,EmpireFeature.FLASH,EmpireFeature.CLEAN->{
                val job=feature.ordinal-EmpireFeature.SCREEN.ordinal+1
                if(get("job")!=job.toLong() || get("diagnosed")!=1L)return reject("Сначала диагностируй соответствующий заказ")
                val parts=when(job){1->if(get("research")>=2)0 else 1;2->1;3->if(get("research")>=1)1 else 2;else->0}
                if(get("parts")<parts)return reject("Нужно деталей: $parts")
                add("parts",-parts.toLong());add("jobs",1);add("rep",1);set("job",0);set("diagnosed",0)
                delta=1500+get("room")*300+get("tools")*250+get("bench")*150+get("quality")*200+if(job==4 && get("research")>=3)1000 else 0
                message="Заказ выполнен: ${faults[job-1]}"
            }
            EmpireFeature.PARTS->{if(get("parts")>95)return reject("На складе мало места");add("parts",5);delta=-2000;message="Куплено 5 деталей"}
            EmpireFeature.RECYCLE->{
                if(choice !in p.owned.indices)return reject("Выбери телефон-донор")
                if(Catalog.isUltra(p.owned[choice]) || p.traitsAt(choice).mutation!=Mutation.NORMAL)return reject("Этот экземпляр защищён от разборки")
                if(get("parts")>97)return reject("Освободи место на складе")
                next=p.removePhoneAt(choice);add("parts",3);message="Разобран ${Catalog.phones[p.owned[choice]].name}"
            }
            EmpireFeature.PARCEL->{val pool=Catalog.phones.indices.filter { !Catalog.isUltra(it) && Catalog.phones[it].price<=5000 }
                val model=pool[(get("parcels")%pool.size).toInt()];delta=-6000
                next=p.appendPhone(model,PhoneTraits(condition=0));if(next==p)return reject("Не удалось создать экземпляр")
                add("parcels",1);message="В посылке: ${Catalog.phones[model].name}, состояние — повреждённый"}
            EmpireFeature.DELIVERY->{
                if(get("delivery")==0L){if(get("parts")>90)return reject("Нужно 10 свободных мест");delta=-(3000-get("supplier")*200);set("delivery",now+(60-get("bench")*5)*1000);message="Поставка заказана"}
                else {if(now<get("delivery"))return reject("Курьер прибудет через ${(get("delivery")-now+999)/1000} с")
                    if(get("parts")>90)return reject("Освободи 10 мест; оплаченный заказ сохранён")
                    add("parts",10);set("delivery",0);message="Получено 10 деталей"}
            }
            EmpireFeature.STAFF->{
                if(get("staff")==0L){delta=-15000;set("staff",1);message="Помощник нанят"}
                else if(get("work")==0L){delta=-500;set("work",now+60000);set("workPay",1200+get("training")*200);message="Помощник выполняет поручение"}
                else {if(now<get("work"))return reject("Поручение завершится через ${(get("work")-now+999)/1000} с")
                    delta=get("workPay");set("work",0);set("workPay",0);message="Поручение оплачено"}
            }
            EmpireFeature.ADS->{if(now<get("adAt"))return reject("Реклама перезаряжается: ${(get("adAt")-now+999)/1000} с");delta=-1000;add("rep",2);set("adAt",now+600000);message="Реклама: +2 репутации"}
            EmpireFeature.WARRANTY->{if(get("warranty")>27)return reject("Лимит — 30 талонов");delta=-500;add("warranty",3);message="Получено 3 гарантийных талона"}
            EmpireFeature.DAILY->{val day=now/86400000+1
                if(get("shiftDay")>day)return reject("Дата смены ещё не наступила")
                if(get("shiftDay")!=day){set("shiftDay",day);set("shiftStart",get("jobs"));set("shiftClaim",0);message="Смена начата: выполни три новых заказа"}
                else {if(get("shiftClaim")==1L)return reject("Награда этой смены уже получена")
                    if(get("jobs")-get("shiftStart")<3)return reject("Выполнено ${get("jobs")-get("shiftStart")} из 3 заказов")
                    delta=3000;set("shiftClaim",1);message="Награда за смену"}
            }
            EmpireFeature.STORY->{val chapter=get("chapter");if(chapter>=8)return reject("История завершена — салон стал легендой!")
                if(get("jobs")<(chapter+1)*3 || get("room")<minOf(5,chapter/2+1))return reject("Глава ${chapter+1}: нужно ${(chapter+1)*3} заказов и помещение уровня ${minOf(5,chapter/2+1)}")
                delta=2000*(chapter+1);add("chapter",1);message="Глава ${chapter+1} завершена: ${listOf("Первый клиент","Надёжные руки","Растущая очередь","Новый адрес","Мастер района","Свой салон","Большая сеть","Телефонная империя")[chapter.toInt()]}"}
            EmpireFeature.SHOWCASE->{if(choice !in p.owned)return reject("Телефон должен быть в коллекции");if(get("display")==choice+1L)return reject("Телефон уже в витрине");set("display",choice+1L);message="Витрина: ${Catalog.phones[choice].name}"}
            EmpireFeature.EXHIBITION->{val day=now/86400000+1;if(get("expoDay")>=day)return reject("Следующая выставка завтра UTC")
                if(p.owned.distinct().size<5 || get("rep")<5)return reject("Нужно 5 разных моделей и 5 репутации")
                delta=2000;add("rep",1);set("expoDay",day);message="Выставка проведена: +1 репутации"}
            EmpireFeature.JOURNAL->return reject("Журнал обновляется после действий")
        }
        val balance=p.coins.toLong()+delta
        if(balance !in 0..Int.MAX_VALUE.toLong())return reject(if(balance<0)"Недостаточно денег" else "Освободи место в кошельке; награда сохранена")
        val line=message+if(delta==0L)"" else " · ${if(delta>0)"+" else ""}$delta ₽"
        val state=Empire(v,(e.journal+line).takeLast(40))
        return EmpireResult(next.copy(coins=balance.toInt(),world=next.world.copy(empire=state).bumpEconomy()),line)
    }
}
