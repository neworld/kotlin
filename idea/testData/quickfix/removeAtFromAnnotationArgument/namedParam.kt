// "Remove @ from annotation argument" "true"
// ERROR: An annotation parameter must be a compile-time constant

annotation class Y()
annotation class X(val value: Y, val y: Y)

@X(Y(), y = @Y()<caret>)
fun foo() {

}