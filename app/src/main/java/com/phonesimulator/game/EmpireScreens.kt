package com.phonesimulator.game

import android.app.AlertDialog
import android.widget.LinearLayout
import android.widget.Toast

internal object EmpireScreens {
    fun render(host: MainActivity,route:String){
        val p=host.gameState;val e=p.world.empire
        fun text(s:String)=host.label(s)
        fun button(s:String,action:()->Unit)=host.button(s,action=action)
        fun open(f:EmpireFeature)=host.worldGo("new_${f.ordinal}")
        fun act(f:EmpireFeature,choice:Int=0){
            val result=EmpireRules.act(host.gameState,f,choice)
            if(result.progress!=host.gameState && host.worldWrite(result.progress,result.message))host.worldGo(route)
            Toast.makeText(host,result.message,Toast.LENGTH_LONG).show()
        }
        val f=route.removePrefix("new_").toIntOrNull()?.let { EmpireFeature.entries.getOrNull(it) }
        host.screen(if(f==null)"Новое · 4.5" else f.title)
        if(f==null){
            text("От мастерской до телефонной империи")
            text("Помещение ${e["room"]}/5 · заказы ${e["jobs"]} · репутация ${e["rep"]} · детали ${e["parts"]}/100")
            text("Начни с диагностики. Выбери верный диагноз, затем открой подходящий ремонт. Для первых заказов понадобятся детали со склада.")
            EmpireFeature.entries.forEachIndexed { i,item -> button("${i+1}. ${item.title}"){open(item)} }
            return
        }
        text(f.description)
        text("Баланс: ${p.coins} ₽ · детали: ${e["parts"]}/100 · репутация: ${e["rep"]}")
        when(f){
            EmpireFeature.ROOM,EmpireFeature.TOOLS,EmpireFeature.BENCH,EmpireFeature.QUALITY,EmpireFeature.SUPPLIER,EmpireFeature.TRAINING,EmpireFeature.RESEARCH->{
                val key=when(f){EmpireFeature.ROOM->"room";EmpireFeature.TOOLS->"tools";EmpireFeature.BENCH->"bench";EmpireFeature.QUALITY->"quality";EmpireFeature.SUPPLIER->"supplier";EmpireFeature.TRAINING->"training";else->"research"}
                val base=when(f){EmpireFeature.ROOM->5000;EmpireFeature.TOOLS->2000;EmpireFeature.BENCH->2500;EmpireFeature.QUALITY,EmpireFeature.TRAINING->3000;EmpireFeature.SUPPLIER->4000;else->6000}
                val max=if(key in listOf("supplier","research"))3 else 5
                text("Уровень ${e[key]} / $max")
                if(e[key]<max)button("Улучшить · ${base*(e[key]+1)} ₽"){act(f)}
                if(f==EmpireFeature.ROOM){
                    text(listOf("Рабочий стол","Гараж","Мастерская","Салон","Флагманский салон","Сеть салонов")[e[key].toInt().coerceIn(0,5)])
                    val model=(e["display"]-1).toInt()
                    if(model in p.owned){text("На витрине: ${Catalog.phones[model].name}");host.worldRoot.addView(PhoneArtView(host,Catalog.phones[model]),LinearLayout.LayoutParams(-1,(180*host.resources.displayMetrics.density).toInt()))}
                    else button("Выбрать витрину"){open(EmpireFeature.SHOWCASE)}
                }
            }
            EmpireFeature.DIAGNOSIS->{
                val job=e["job"].toInt()
                if(job !in 1..5)button("Принять заказ"){act(f)}
                else {
                    text(EmpireRules.symptoms[job-1])
                    if(e["diagnosed"]==1L){text("Диагноз: ${EmpireRules.faults[job-1]}");button("Перейти к ремонту"){open(EmpireFeature.entries[EmpireFeature.SCREEN.ordinal+job-1])}}
                    else EmpireRules.faults.forEachIndexed { i,name->button("Проверить: $name"){act(f,i+1)} }
                }
            }
            EmpireFeature.SCREEN,EmpireFeature.BATTERY,EmpireFeature.CHARGE,EmpireFeature.FLASH,EmpireFeature.CLEAN->{
                text("Активный заказ: ${EmpireRules.faults.getOrNull(e["job"].toInt()-1) ?: "нет"}")
                text("Выполнено заказов: ${e["jobs"]}")
                button("Выполнить ремонт"){act(f)};button("Диагностика"){open(EmpireFeature.DIAGNOSIS)};button("Купить запчасти"){open(EmpireFeature.PARTS)}
            }
            EmpireFeature.PARTS->button("Купить 5 деталей · 2000 ₽"){act(f)}
            EmpireFeature.RECYCLE->{
                val eligible=p.owned.indices.filter { !Catalog.isUltra(p.owned[it]) && p.traitsAt(it).mutation==Mutation.NORMAL }
                if(eligible.isEmpty())text("Нет подходящих доноров")
                eligible.take(50).forEach { pos->button("Разобрать: ${Catalog.phones[p.owned[pos]].name}"){
                    AlertDialog.Builder(host).setTitle("Разобрать телефон?").setMessage("Этот экземпляр исчезнет из коллекции. Получишь 3 детали.")
                        .setNegativeButton("Отмена",null).setPositiveButton("Разобрать"){_,_->act(f,pos)}.show()
                } }
                if(eligible.size>50)text("Показаны первые 50 доноров. После разборки список обновится.")
            }
            EmpireFeature.PARCEL->button("Купить посылку · 6000 ₽"){act(f)}
            EmpireFeature.DELIVERY->{
                text(if(e["delivery"]==0L)"Нет активной поставки" else "До прибытия: ${((e["delivery"]-System.currentTimeMillis()+999)/1000).coerceAtLeast(0)} с")
                button(if(e["delivery"]==0L)"Заказать · ${3000-e["supplier"]*200} ₽" else "Получить поставку"){act(f)}
            }
            EmpireFeature.STAFF->{
                text("Обучение: ${e["training"]}/5")
                if(e["work"]>0)text("До завершения: ${((e["work"]-System.currentTimeMillis()+999)/1000).coerceAtLeast(0)} с · выручка ${e["workPay"]} ₽")
                button(when{e["staff"]==0L->"Нанять · 15000 ₽";e["work"]==0L->"Дать поручение · 500 ₽";else->"Забрать выручку"}){act(f)}
            }
            EmpireFeature.ADS->{text("До новой кампании: ${((e["adAt"]-System.currentTimeMillis()+999)/1000).coerceAtLeast(0)} с");button("Запустить рекламу · 1000 ₽"){act(f)}}
            EmpireFeature.WARRANTY->{text("Талонов: ${e["warranty"]}/30");button("Купить 3 талона · 500 ₽"){act(f)}}
            EmpireFeature.DAILY->{text("Прогресс смены: ${(e["jobs"]-e["shiftStart"]).coerceIn(0,3)}/3 · награда ${if(e["shiftClaim"]==1L)"получена" else "ожидает"}");button("Начать смену / забрать 3000 ₽"){act(f)}}
            EmpireFeature.STORY->{text("Завершено глав: ${e["chapter"]}/8");text("Следующая цель: ${(e["chapter"]+1)*3} заказов и помещение уровня ${minOf(5,e["chapter"]/2+1)}");button("Завершить главу"){act(f)}}
            EmpireFeature.SHOWCASE->{
                if(p.owned.isEmpty())text("Сначала получи телефон")
                val models=p.owned.distinct();var shown=0
                lateinit var more: android.widget.Button
                fun next(){models.drop(shown).take(30).forEach { model->button("На витрину: ${Catalog.phones[model].name}"){act(f,model)} };shown=minOf(shown+30,models.size);more.visibility=if(shown<models.size)android.view.View.VISIBLE else android.view.View.GONE}
                more=button("Ещё модели"){next()};next()
            }
            EmpireFeature.EXHIBITION->{text("Разных моделей: ${p.owned.distinct().size} / 5 · репутация ${e["rep"]} / 5");button("Провести выставку · +2000 ₽"){act(f)}}
            EmpireFeature.JOURNAL->{if(e.journal.isEmpty())text("Пока нет операций");e.journal.asReversed().forEach { text(it) }}
        }
        button("Обновить"){host.worldGo(route)}
        button("Все 25 функций"){host.worldGo("new")}
    }
}
