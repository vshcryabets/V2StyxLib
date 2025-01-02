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
styxQidType.addBlockComment("QID record types, the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.")
styxQidType.defaultType(DataType.int32.INSTANCE)
styxQidType.add("QTDIR", 0x80)
styxQidType.add("QTAPPEND", 0x40)
styxQidType.add("QTEXCL", 0x20)
styxQidType.add("QTMOUNT", 0x10)
styxQidType.add("QTAUTH", 0x08)
styxQidType.add("QTTMP", 0x04)
styxQidType.add("QTSYMLINK", 0x02)
styxQidType.add("QTLINK", 0x01)
styxQidType.add("QTFILE", 0x00)

//def styxQid = ns.dataClass("StyxQID")

//def stat = ns.dataClass("StyxStat")
//stat.field("type", DataType.int32.INSTANCE)
//stat.field("dev", DataType.int64.INSTANCE)