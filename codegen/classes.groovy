import static ce.defs.FuncsKt.target
import static ce.defs.FuncsKt.namespace
import static ce.defs.FuncsKt.setOutputBasePath
import ce.defs.DataType

def nsL5 = namespace("styxlib")
switch (target()) {
    case ce.defs.Target.Kotlin:
    case ce.defs.Target.Java:
        nsL5 = namespace("com.v2soft.styxlib.l5")
        break;
    case ce.defs.Target.Cpp:
        setOutputBasePath("../native/V2StyxLib/modules/l5/")
        break;
}

def nsEnums = nsL5.getNamespace("enums")
def nsStructs = nsL5.getNamespace("structs")

def modeType = nsEnums.constantsBlock("ModeType")
modeType.setOutputFile("include/enums/ModeType")
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
messageType.setOutputFile("include/enums/MessageType")
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

def fileMode = nsEnums.constantsBlock("FileMode")
fileMode.setOutputFile("include/enums/FileMode")
fileMode.with {
    defaultType(DataType.int64.INSTANCE)
    preferredRadix(16)
    add("Directory",0x80000000L)
    add("AppendOnly",0x40000000L)
    add("ExclusiveUse",0x20000000L)
    add("MountedChannel",0x10000000L)
    add("AuthenticationFile",0x08000000L)
    add("TemporaryFile",0x04000000L)
    add("SymLinkFile",0x02000000)
    add("LinkFile",0x01000000)
    add("DeviceFile",0x00800000)
    add("NamedPipeFile",0x00200000)
    add("SocketFile",0x00100000)
    add("ReadOwnerPermission",0x00000100L)
    add("WriteOwnerPermission",0x00000080L)
    add("ExecuteOwnerPermission",0x00000040L)
    add("ReadGroupPermission",0x00000020L)
    add("WriteGroupPermission",0x00000010L)
    add("ExecuteGroupPermission",0x00000008L)
    add("ReadOthersPermission",0x00000004L)
    add("WriteOthersPermission",0x00000002L)
    add("ExecuteOthersPermission",0x00000001L)
    add("PERMISSION_BITMASK", 0x000001FFL)
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

def nsLibraryTypes = namespace("styxlib.types")
switch (target()) {
    case ce.defs.Target.Kotlin:
    case ce.defs.Target.Java:
        nsLibraryTypes = namespace("com.v2soft.styxlib.library.types")
}
def connectionDetails = nsLibraryTypes.dataClass("ConnectionDetails")
connectionDetails.with {
    addBlockComment("Styx connection details")
    field("protocol", new DataType.string())
    field("ioUnit", DataType.int32.INSTANCE)
}
//def stat = ns.dataClass("StyxStat")
//stat.field("type", DataType.int32.INSTANCE)
//stat.field("dev", DataType.int64.INSTANCE)

