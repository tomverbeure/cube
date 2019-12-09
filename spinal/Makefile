
all: syn

#sim:
#	sbt "test-only rt.PanoTester"

.PHONY: submodule
submodule:
	cd ../ && git submodule update --init

syn: submodule
	sbt "runMain cube.CubeTopVerilogSyn"
	mv CubeTop.v CubeTop.syn.v

sim: submodule
	sbt "runMain cube.CubeTopVerilogSim"
	mv CubeTop.v CubeTop.sim.v


waves:
	gtkwave -o simWorkspace/PanoCoreDut/test.vcd &
    
