package jp.digital_future.cameraxsample.flux

interface Action<out T> {
    val data: T
}

interface Action2<out T, out N> {
    val data1: T
    val data2: N
}

interface Action3<out T, out N, out G> {
    val data1: T
    val data2: N
    val data3: G
}