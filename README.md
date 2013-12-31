Faster Garbled Circuit
==========
This framework helps accelerate garbled circuits.


## Instructions

Type "ant archive" to compile the code.

To run the hamming distance application, type:

    ant runhamming
    
It invokes the "runhamming" bash script to run both the server and the client.
The output goes to the file ** results/hammingserverout **.

To run the Levenshtein distance application, type:

    ant runlevenshtein

Similarly, the output goes to the file results/levenshteinserverout.

To run the Smith-Waterman application, type

    ant runsw

Similarly, the output goes to the file results/swserverout.

To run the secure AES, type:

    ant runaes

Similarly, the output goes to the file results/aesserverout.


## Authors
* Yan Huang, University of Virgina

* Wei Xie, Vanderbilt University


## Licence

This software package is made freely available under the MIT license.

Copyright (C) 2010 by Yan Huang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.