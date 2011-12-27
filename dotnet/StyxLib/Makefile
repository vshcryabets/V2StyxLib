CSC=gmcs
FILES =  \
	*.cs \
	FileSystem/*.cs \
	Messages/*.cs \
	Messages/Structures/*.cs \
	Streams/*.cs \
	Interfaces/*.cs \
	Server/*.cs \
	Utils/*.cs
DLL=StyxLib.dll
OUTDIR=../out/
REFS =

all: $(DLL) documentation

$(DLL): $(FILES)
	[ ! -d $(OUTDIR) ] && mkdir $(OUTDIR) || echo Directory exists
	$(CSC) -target:library -out:$(OUTDIR)$(DLL) $(FILES) $(REFS)

documentation:
	doxygen default.dox

clean:
	rm -r $(OUTDIR)