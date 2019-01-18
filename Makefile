#
# Qudini Task's Makefile
#
#
# Requires POSIX make; depending on complex POSIX shell commands is discouraged
# but permissable. Using non-POSIX features, e.g. GNU Make features,
# is _not_ acceptable.
#

.POSIX:

#
# Overriddable Arguments
#

MAVEN=mvn
MAVEN_FLAGS=

#
# Standard Targets
#

all:
	$(MAVEN) compile $(MAVEN_FLAGS)

help:
	@echo 'Makefile for Qudini Tasks                               '
	@echo '                                                        '
	@echo 'Usage:                                                  '
	@echo '   make        Build for production.                    '
	@echo '   make check  Run the tests.                           '
	@echo "   make clean  Clear out caches and temporary artefacts."
	@echo '   make dist   Create a JAR artefact for deployment.    '

check:
	$(MAVEN) verify $(MAVEN_FLAGS)

clean:
	$(MAVEN) clean $(MAVEN_FLAGS)

dist:
	$(MAVEN) package $(MAVEN_FLAGS)
