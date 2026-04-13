package com.example.swiggyclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay

// MODEL
data class Product(
    val name:String,
    val price:Int,
    val img:Int,
    val rating:Double,
    val time:String,
    val discount:String
)

// PRODUCTS (ALL)
fun getProducts() = listOf(
    Product("Chicken",269,R.drawable.chiken,4.5,"20-25 mins","20% OFF"),
    Product("Chole",135,R.drawable.chole,4.2,"25-30 mins","15% OFF"),
    Product("Coffee",256,R.drawable.coffee,4.6,"10-15 mins","10% OFF"),
    Product("Dosa",199,R.drawable.dhosa,4.3,"20 mins","25% OFF"),
    Product("Eggs",150,R.drawable.eggs,4.1,"15 mins","10% OFF"),
    Product("Fish",300,R.drawable.fish,4.4,"30 mins","30% OFF"),
    Product("Fries",120,R.drawable.fries,4.0,"10 mins","15% OFF"),
    Product("Lassi",90,R.drawable.lassi,4.7,"5 mins","5% OFF"),
    Product("Milkshake",180,R.drawable.milkshake,4.5,"10 mins","20% OFF"),
    Product("Momos",110,R.drawable.momos,4.3,"15 mins","25% OFF"),
    Product("Noodles",200,R.drawable.noodals,4.2,"20 mins","20% OFF"),
    Product("Pasta",240,R.drawable.pasta,4.4,"25 mins","30% OFF"),
    Product("Prawn",320,R.drawable.prawn,4.6,"30 mins","35% OFF"),
    Product("Rajma",170,R.drawable.rajma,4.1,"20 mins","15% OFF"),
    Product("Sandwich",130,R.drawable.sandvich,4.3,"15 mins","10% OFF"),
    Product("Tea",50,R.drawable.tea,4.8,"5 mins","5% OFF")
)

// CART
object CartData{
    val items= mutableStateListOf<Product>()
    val qty= mutableStateMapOf<String,Int>()

    fun add(p:Product){
        qty[p.name]=(qty[p.name]?:0)+1
        if(!items.contains(p)) items.add(p)
    }

    fun remove(p:Product){
        val q=qty[p.name]?:return
        if(q<=1){qty.remove(p.name);items.remove(p)}
        else qty[p.name]=q-1
    }

    fun clear(){items.clear();qty.clear()}

    val total get()=items.sumOf{(qty[it.name]?:1)*it.price}
    val count get()=qty.values.sum()
}

// HISTORY
object HistoryData{
    val orders= mutableStateListOf<List<Product>>()
}

// MAIN
class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent{App()}
    }
}

// NAV
@Composable
fun App(){
    val nav= rememberNavController()
    NavHost(nav,startDestination="home"){
        composable("home"){Home(nav)}
        composable("cart"){Cart(nav)}
        composable("payment"){Payment(nav)}
        composable("history"){History()}
    }
}

// HOME UI (LIKE SCREENSHOT)
@Composable
fun Home(nav:NavHostController){
    val list=getProducts()

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF2F2F2))
    ){

        // TOP
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF7B1FA2))
                .padding(16.dp)
        ){
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){
                Text("Mumbai, India", color=Color.White)
                Button(onClick={nav.navigate("cart")}){
                    Text("Cart (${CartData.count})")
                }
            }

            Spacer(Modifier.height(10.dp))

            TextField(
                value="",
                onValueChange={},
                placeholder={Text("Search 'Biryani'")},
                modifier=Modifier.fillMaxWidth()
            )
        }

        LazyColumn{
            items(list){p-> ProductCard(p)}
        }
    }
}

// PRODUCT CARD (MATCH UI)
@Composable
fun ProductCard(p:Product){

    Card(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
    ){
        Column{

            Box{
                Image(
                    painterResource(p.img),
                    null,
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    Modifier.padding(10.dp)
                        .background(Color.Black.copy(0.7f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ){
                    Text(p.discount,color=Color.White)
                }
            }

            Column(Modifier.padding(12.dp)){

                Text(p.name,fontSize=18.sp)
                Text("⭐ ${p.rating} • ${p.time}", color=Color.Gray)
                Text("₹${p.price}")

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick={CartData.add(p)},
                    modifier=Modifier.fillMaxWidth(),
                    colors=ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
                ){
                    Text("ADD")
                }
            }
        }
    }
}

// CART
@Composable
fun Cart(nav:NavHostController){
    Column(Modifier.fillMaxSize()){

        LazyColumn(Modifier.weight(1f)){
            items(CartData.items){p->
                val q=CartData.qty[p.name]?:1
                Text("${p.name} x$q = ₹${p.price*q}", Modifier.padding(10.dp))
            }
        }

        Column(Modifier.padding(16.dp)){
            Text("Total ₹${CartData.total}")

            Button(
                onClick={nav.navigate("payment")},
                modifier=Modifier.fillMaxWidth()
            ){
                Text("Proceed to Payment")
            }
        }
    }
}

// PAYMENT
@Composable
fun Payment(nav:NavHostController){

    var success by remember{ mutableStateOf(false) }

    if(success){
        LaunchedEffect(Unit){
            delay(2000)

            val order = CartData.items.flatMap { p ->
                List(CartData.qty[p.name]?:1){p}
            }

            HistoryData.orders.add(order)
            CartData.clear()

            nav.navigate("history")
        }

        Column(
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ){
            Icon(Icons.Default.CheckCircle,null, tint=Color.Green, modifier=Modifier.size(100.dp))
            Text("Payment Successful")
        }
        return
    }

    Column(Modifier.padding(16.dp)){
        Text("Select Payment")

        Button(onClick={success=true}){
            Text("Pay ₹${CartData.total}")
        }
    }
}

// HISTORY
@Composable
fun History(){
    Column(Modifier.padding(16.dp)){
        Text("Order History")

        if(HistoryData.orders.isEmpty()){
            Text("No Orders")
        }else{
            LazyColumn{
                items(HistoryData.orders){order->
                    Card(Modifier.padding(8.dp)){
                        Column(Modifier.padding(10.dp)){
                            order.forEach{
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        }
    }
}