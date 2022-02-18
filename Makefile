BINDIR=bin
SRCDIR=src
MVNDIR=target
OUTNAME=pick_up
JARNAME=bot-pick-up-jar-with-dependencies

build:
	mkdir -p $(BINDIR)
	mvn package
	cp $(MVNDIR)/$(JARNAME).jar $(BINDIR)/$(OUTNAME).jar

clean:
	rm -r target

