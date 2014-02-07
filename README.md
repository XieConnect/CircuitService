# Garbled Circuit Backend Service

This package turns garbled circuit evaluation into an always-online backend service. This way we can split the online phase from the offline one when evaluating a circuit, and can re-use the circuit preparation across a series of computations.

We also provide a few optimized subcircuits for certain computations. The low-level circuit evaluation is based on Yan Huang's FastGC framework (CCS'11).


## Instructions

Type "ant archive" to compile the code.

To run the hamming distance application, type:

    ant runhamming
    
It invokes the "runhamming" bash script to run both the server and the client.
The output goes to the file <i> results/hammingserverout </i>.

For the Levenshtein distance application, the corresponding ant task is: <i>runlevenshtein</i> ; and for the Smith-Waterman application, it is: <i>runsw</i> ; for secure AES, it is: <i>runaes</i>. 

To run the secure ln(x) application, type:

    ant runln


## Our Enhancements
- Split online and offline phases of circuit evaluation;
- Run online phase as a backend service, constantly taking inputs from the net;
- Customized circuits for a few computations;
- Allow for returning multi-value results from circuit;
- Added necessary documentation to source code;
- etc.


## TO-DO
- Parallel execution on subcircuit level;
- Faster OT protocol;


## Copyright & Licence
Refer to LICENSE file and authors.


## Authors
* Wei Xie <XieConnect@gmail.com>

* Yan Huang, University of Virgina  (author of original FastGC)