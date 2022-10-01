import ce.defs.*
import generators.obj.input.*

when (target()) {
    ce.defs.Target.Kotlin, ce.defs.Target.Java -> namespace("com.v2soft.styxlib.l5.enums")
    else -> namespace("styxlib")
}

setOutputBasePath("../java/library/src/main/java/")
constantsBlock("ModeType").apply {
    addBlockComment("File mode types")
    defaultType(DataType.int32)
    add("OREAD", 0)
    add("OWRITE", 1)
    add("ORDWR", 2)
    add("OEXEC", 3)
    add("OTRUNC", 0x10)
}
