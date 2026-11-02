package com.phonesimulator.game
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import kotlin.random.Random
class MainActivity : Activity() {
 private val phones=listOf("Nokia 3310","Redmi Note","Google Pixel","Samsung Galaxy S","iPhone Pro","Samsung Z Fold 8 Ultra")
 private val prices=listOf(50,150,500,1200,2500,25000)
 private val prefs by lazy { getSharedPreferences("game",MODE_PRIVATE) }
 private var coins=1000
 private var owned=mutableListOf<Int>()
 private lateinit var root:LinearLayout
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); coins=prefs.getInt("coins",1000); owned=prefs.getString("owned","")!!.split(",").mapNotNull{it.toIntOrNull()}.toMutableList(); home() }
 private fun save(){prefs.edit().putInt("coins",coins).putString("owned",owned.joinToString(",")).apply()}
 private fun label(s:String,size:Float=20f){root.addView(TextView(this).apply{text=s;textSize=size;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setPadding(12,20,12,20)})}
 private fun screen(s:String){val sc=ScrollView(this).apply{setBackgroundColor(Color.rgb(15,12,34))};root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,32,24,24)};sc.addView(root);setContentView(sc);label("📱 PHONE CASE SIMULATOR",25f);label(s,27f);label("🪙 $coins монет")}
 private fun button(s:String,action:()->Unit){root.addView(Button(this).apply{text=s;isAllCaps=false;textSize=17f;setOnClickListener{action()}},LinearLayout.LayoutParams(-1,110))}
 private fun home(){screen("Открывай кейсы!");label("Собери все телефоны! Z Fold 8 Ultra — шанс 0,1%");button("🎁 Открыть кейс — 100 монет"){openCase()};button("📦 Коллекция"){collection()};button("🛒 Магазин"){shop()}}
 private fun openCase(){if(coins<100){Toast.makeText(this,"Недостаточно монет",Toast.LENGTH_SHORT).show();return};coins-=100;val r=Random.nextInt(1000);val i=when{r==0->5;r<12->4;r<70->3;r<220->2;r<500->1;else->0};owned.add(i);save();screen("Кейс открыт!");label("🎉 "+phones[i],30f);if(i==5)label("🌟 УЛЬТРАЛЕГЕНДАРНЫЙ! 🌟");button("Ещё кейс"){openCase()};button("Главная"){home()}}
 private fun collection(){screen("Моя коллекция");if(owned.isEmpty())label("Пока пусто");owned.groupingBy{it}.eachCount().toSortedMap().forEach{(i,n)->label(phones[i]+" × "+n);button("Продать за "+prices[i]){owned.remove(i);coins+=prices[i];save();collection()}};button("Назад"){home()}}
 private fun shop(){screen("Магазин");label("Бонус: 500 монет");button("Получить бонус"){coins+=500;save();home()};button("Назад"){home()}}
}