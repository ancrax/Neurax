Neurax
======

Multidimensional reucrrent neural network to play Go.

Neurax can learn from played games. It support Smart Game Format storing. Comunication with GUI or server is realized by Go Text Protocol v2. Neural network has three layers (four with input layer). First hidden layer is multidimensional recurrent layer. Size of this layer is same as board size. Second hidden and output layer is standard layers with full conection of neurons between layers. As a learning method is implement Backpropagation with adaptiv dynamic.
Feel free to copy, fork and share.

This content is release by WTFPL licence (any version)


-----------------------------------------------
How to use:
file dist/Neurax.jar contains java bytecode
file dist/EngineConfig.bin contain neural configuration (learned samples)

to run type "java -jar <path to Neurax.jar>"
Notice - check if config file is in same folder. If not, new network config will be created.
Notice 2 - param debug=true switch on debug mode

For control type "list_commands" or see Go Text Protocol manula
For learning run command "neurax_learn_samples path" where param path is path to .sgf files


Please fell free to report bug or something at <ancrax [at] yandex [dot] com>. Thanks :)

