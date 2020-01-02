
all: syn

#sim:
#	sbt "test-only rt.PanoTester"

.PHONY: submodule
submodule:
	cd ../ && git submodule update --init

sw:
	cd ../sw && make

syn: submodule sw
	sbt "runMain cube.CubeShimVerilogSyn"
	mv CubeShim.v CubeShim.syn.v

sim: submodule sw
	sbt "runMain cube.CubeTopVerilogSim"
	mv CubeTop.v CubeTop.sim.v


waves:
	gtkwave -o simWorkspace/PanoCoreDut/test.vcd &
    
