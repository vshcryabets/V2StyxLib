CSC=gmcs
FILES=*.cs
DLL=ClientTest.exe
OUTDIR=../out/
REFS=-r:$(OUTDIR)StyxLib.dll

all: $(DLL)

$(DLL): $(FILES)
	[ ! -d $(OUTDIR) ] && mkdir $(OUTDIR) || echo Directory exists
	$(CSC) -target:exe -out:$(OUTDIR)$(DLL) $(FILES) $(REFS)

clean:
	rm -r $(OUTDIR)