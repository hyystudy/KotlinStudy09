fun main(args: Array<String>) {

    //函数类型 类型推导
    val sum = { x: Int, y: Int -> x + y}//有俩个Int型参数和Int返回值的函数
    val action = { println(42) }//没有参数类型和返回值的函数
    //函数类型语法:
    //参数类型放在括号里 一个箭头  函数返回值
    //(Int, String)  -> Unit
    val canReturnNull: (Int, Int) -> Int? = { x, y -> null }


    //显示声明
    val sum2: (Int, Int) -> Int = { x, y -> x + y }
    val action2: () -> Unit = { println(42) }//Unit 也不能省略


    //简单高阶函数 参数或者返回值 有lambda或者函数引用的
    twoAndThree(sum)
    twoAndThree{ x, y -> x + y }
    twoAndThree{ x, y -> x * y }

    val  predicate: (Char) -> Boolean = { it in 'a'..'z' }
    println("ab1c".filter(predicate))
    println("ab1c".filter { it in 'a'..'z' })

    val names = listOf("hyy", "zal")
    println(names.joinToString())//使用默认转换函数
    println(names.joinToString { it.toUpperCase() })
    //使用命名参数语法传递参数
    println(names.joinToString(separator = ", " , prefix = "[", postfix = "]", transform = { it.decapitalize() }))


    //返回函数的函数
    val shippingCostCalculator = getShippingCostCalculator(Delivery.EXPEDITED)

    println("Shipping costs ${shippingCostCalculator(Order(4))}")

    val contacts = listOf(Person("Dmitry", "Jemerov", "123-4567"),
            Person("h", "yy", null))

    val contactListFilters = ContactListFilters()
    //定义过滤条件
    with(contactListFilters){
        contactListFilters.prefix = "Dm"
        contactListFilters.onlyWithPhoneNumber = true
    }

    println(contacts.filter(contactListFilters.getPredicate()))


    val log = listOf(
            SiteVisite("/", 34.0, OS.WINDOWS),
            SiteVisite("/", 22.0, OS.MAC),
            SiteVisite("/login", 12.0, OS.WINDOWS),
            SiteVisite("/signup", 8.0, OS.IOS),
            SiteVisite("/", 16.3, OS.ANDROID)
    )

    //显示 window用户平均访问时长
    val averageWindowDuration = log
            .filter { it.os == OS.WINDOWS }
            .map { it.duration }
            .average()
    println(averageWindowDuration)

    //显示 mac用户平均访问时长
    val averageMacDuration = log
            .filter { it.os == OS.MAC }
            .map { it.duration }
            .average()

    println(averageMacDuration)
    println(log.averageDuration(OS.WINDOWS))
    println(log.averageDuration(OS.MAC))

    //显示移动平台的平均访问时长
    val averagemobileDuration = log
            .filter { it.os in setOf(OS.IOS, OS.ANDROID) }
            .map { it.duration }
            .average()

    println(averagemobileDuration)

    //使用lambda去掉重复代码后 只需要传入相应的过滤条件就行了
    println(log.averageDuration { it.os in setOf(OS.IOS, OS.ANDROID) })
    println(log.averageDuration { it.os == OS.IOS && it.path == "/signup" })
}


fun twoAndThree(operation: (Int, Int) -> Int) {
    val result = operation(2, 3)
    println("Result is $result")
}

//filter 函数的实现
fun String.filter(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()

    for (index in 0 until length) {
        val element = get(index)

        val contains = predicate(element)
        println(contains)
        if (contains) sb.append(element)
    }
    println(sb.toString())
    return sb.toString()
}

//硬编码joinTOString
fun <T> Collection<T>.joinToString(
        separator: String = ", ",
        prefix: String = "",
        postfix: String = ""
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)

    return result.toString()
}

//给函数类型指定默认参数
fun <T> Collection<T>.joinToString(
        separator: String = ", ",
        prefix: String = "",
        postfix: String = "",
        transform: (T) -> String = { it.toString() }//函数类型的参数
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in withIndex()) {
        if (index > 0) result.append(separator)
        result.append(transform(element))
    }

    result.append(postfix)

    return result.toString()
}

//函数类型 可为null
fun <T> Collection<T>.joinToStringSafe(
        separator: String = ", ",
        prefix: String = "",
        postfix: String = "",
        transform: ((T) -> String)? = null//函数类型的参数
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in withIndex()) {
        if (index > 0) result.append(separator)
        val str = transform?.invoke(element) ?: element.toString()
        result.append(str)
    }

    result.append(postfix)

    return result.toString()
}


enum class Delivery { STANDARD, EXPEDITED }

class Order(val itemCount: Int)

//返回函数的函数
fun getShippingCostCalculator(delivery: Delivery): (Order) -> Double {

    if (delivery == Delivery.EXPEDITED) {
        return {order -> 6 + 2.1 * order.itemCount }
    }

    return { order -> 2.1 * order.itemCount }
}

data class Person(
        val firstName: String,
        val secondName: String,
        val phoneNumber: String?
)

class ContactListFilters() {
    var prefix: String = ""
    var onlyWithPhoneNumber: Boolean = false

    fun getPredicate(): (Person) -> Boolean {
        val startsWithPrefix = { p: Person ->
            p.firstName.startsWith(prefix) || p.secondName.startsWith(prefix)
        }

        if (!onlyWithPhoneNumber) {
            return startsWithPrefix
        }

        return { startsWithPrefix(it) && it.phoneNumber != null}
    }
}

//定义站点访问数据
data class SiteVisite(
        val path: String,
        val duration: Double,
        val os: OS
)

enum class OS{ WINDOWS, LINUX, MAC, IOS, ANDROID}

//普通方法去除重复代码
fun List<SiteVisite>.averageDuration(os: OS) =
        filter { it.os == os }.map(SiteVisite::duration).average()

//使用高阶函数(lambda)除重复代码
fun List<SiteVisite>.averageDuration(
        predicate: (SiteVisite) -> Boolean//函数类型 过滤条件
) = filter(predicate).map(SiteVisite::duration).average()