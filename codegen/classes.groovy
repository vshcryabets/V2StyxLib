import static ce.defs.FuncsKt.target
import static ce.defs.FuncsKt.namespace
import ce.defs.DataType

def nsL5 = namespace("styxlib")
switch (target()) {
    case ce.defs.Target.Kotlin:
    case ce.defs.Target.Java:
        nsL5 = namespace("com.v2soft.styxlib.l5")
}

def nsEnums = nsL5.getNamespace("enums")
def nsStructs = nsL5.getNamespace("structs")

def modeType = nsEnums.constantsBlock("ModeType")
modeType.addBlockComment("File mode types")
modeType.defaultType(DataType.int32.INSTANCE)
modeType.add("OREAD", 0)
modeType.add("OWRITE", 1)
modeType.add("ORDWR", 2)
modeType.add("OEXEC", 3)
modeType.add("OTRUNC", 0x10)

def styxQidType = nsEnums.constantsBlock("QidType")
styxQidType.with {
    addBlockComment("QID record types, the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.")
    defaultType(DataType.int32.INSTANCE)
    add("QTDIR", 0x80)
    add("QTAPPEND", 0x40)
    add("QTEXCL", 0x20)
    add("QTMOUNT", 0x10)
    add("QTAUTH", 0x08)
    add("QTTMP", 0x04)
    add("QTSYMLINK", 0x02)
    add("QTLINK", 0x01)
    add("QTFILE", 0x00)
}

def messageType = nsEnums.constantsBlock("MessageType")
messageType.with {
    defaultType(DataType.int32.INSTANCE)
    add("Unspecified",0)
    add("Tversion",100)
    add("Rversion",101)
    add("Tauth",102)
    add("Rauth",103)
    add("Tattach",104)
    add("Rattach",105)
    add("Rerror",107)
    add("Tflush",108)
    add("Rflush",109)
    add("Twalk",110)
    add("Rwalk",111)
    add("Topen",112)
    add("Ropen",113)
    add("Tcreate",114)
    add("Rcreate",115)
    add("Tread",116)
    add("Rread",117)
    add("Twrite",118)
    add("Rwrite",119)
    add("Tclunk",120)
    add("Rclunk",121)
    add("Tremove",122)
    add("Rremove",123)
    add("Tstat",124)
    add("Rstat",125)
    add("Twstat",126)
    add("Rwstat",127)
}

def styxQid = nsStructs.dataClass("StyxQID")
styxQid.with {
    addBlockComment("Styx QID structure")
    //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
    field("type", DataType.int32.INSTANCE)
    // version number for given path
    field("version", DataType.int64.INSTANCE)
    //the file server's unique identification for the file
    field("path", DataType.int64.INSTANCE)
    // public static final StyxQID EMPTY = new StyxQID(QidType.QTFILE, 0L, 0L);
}

//def stat = ns.dataClass("StyxStat")
//stat.field("type", DataType.int32.INSTANCE)
//stat.field("dev", DataType.int64.INSTANCE)