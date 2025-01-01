import static ce.defs.FuncsKt.target
import static ce.defs.FuncsKt.namespace
import ce.defs.DataType

def ns = namespace("styxlib")
switch (target()) {
    case ce.defs.Target.Kotlin:
    case ce.defs.Target.Java:
        ns = namespace("com.v2soft.styxlib.l5.enums")
}

def modeType = ns.constantsBlock("ModeType")
modeType.addBlockComment("File mode types")
modeType.defaultType(DataType.int32.INSTANCE)
modeType.add("OREAD", 0)
modeType.add("OWRITE", 1)
modeType.add("ORDWR", 2)
modeType.add("OEXEC", 3)
modeType.add("OTRUNC", 0x10)

def styxQidType = ns.constantsBlock("QidType")

def styxQid = ns.dataClass("StyxQID")

def stat = ns.dataClass("StyxStat")
//stat.field("type", DataType.int32.INSTANCE)
//stat.field("dev", DataType.int64.INSTANCE)