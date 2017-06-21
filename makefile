# Makefile for Graph Assignment

LIB = lib
SRCDIR = src/animationSkeletonCode
BINDIR = bin
TESTDIR = test
DOCDIR = doc

CLI = $(LIB)/cli/commons-cli-1.3.1.jar
ASM = $(LIB)/asm/asm-5.0.4.jar:$(LIB)/asm/asm-commons-5.0.4.jar:$(LIB)/asm/asm-tree-5.0.4.jar
JUNIT = $(LIB)/junit/junit-4.12.jar:$(LIB)/junit/hamcrest-core-1.3.jar
JACOCO = $(LIB)/jacoco/org.jacoco.core-0.7.5.201505241946.jar:$(LIB)/jacoco/org.jacoco.report-0.7.5.201505241946.jar:
TOOLS = $(LIB)/tools

JAVAC = javac
JFLAGS = -g -d $(BINDIR) -cp $(BINDIR):$(JUNIT)


vpath %.java $(SRCDIR)/graph:$(SRCDIR):$(SRCDIR)/utils:$(TESTDIR)
vpath %.class $(BINDIR)/graph:$(BINDIR)/utils:$(BINDIR)

# define general build rule for java sources
.SUFFIXES:  .java  .class

.java.class:
	$(JAVAC)  $(JFLAGS)  $<

#default rule - will be invoked by make
all: GridBlock.class Person.class RoomGrid.class RoomPanel.class PeopleCounter.class CounterDisplay.class PersonMover.class PartyApp.class
	
PersonMover.class: PartyApp.class

PartyApp.class:
	rm -rf $(BINDIR)/PartyApp.class $(BINDIR)/PersonMover.class
	javac $(JFLAGS) $(SRCDIR)/PartyApp.java $(SRCDIR)/PersonMover.java
# Rules for generating documentation
doc: all
	javadoc -d $(DOCDIR) -link http://docs.oracle.com/javase/8/docs/api/ $(SRCDIR)/*.java 
# Rules for unit testing
# Add additional Testxx.class file to this line and to TestSuite.java


clean:
	@echo "Removing class files, coverage traces and reports."
	@rm -f  $(BINDIR)/*.class
	@rm -f  $(BINDIR)/*/*.class
	@rm -Rf doc
