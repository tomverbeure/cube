
all: syn

#sim:
#	sbt "test-only rt.PanoTester"

.PHONY: submodule
submodule:
	cd ../ && git submodule update --init

syn: submodule
	sbt "runMain cube.CubeShimVerilogSyn"
	mv CubeShim.v CubeShim.syn.v

sim: submodule
	sbt "runMain cube.CubeTopVerilogSim"
	mv CubeTop.v CubeTop.sim.v


waves:
	gtkwave -o simWorkspace/PanoCoreDut/test.vcd &
    
