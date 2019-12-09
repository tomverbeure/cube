
all: syn

#sim:
#	sbt "test-only rt.PanoTester"

.PHONY: submodule
submodule:
	cd ../ && git submodule update --init

syn: submodule
	sbt "runMain led_matrix.LedMatrixTopVerilogSyn"
	mv LedMatrixTop.v LedMatrixTop.syn.v

sim: submodule
	sbt "runMain led_matrix.LedMatrixTopVerilogSim"
	mv LedMatrixTop.v LedMatrixTop.sim.v


waves:
	gtkwave -o simWorkspace/PanoCoreDut/test.vcd &
    
