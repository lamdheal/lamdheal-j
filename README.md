lamdheal
==========
Lamdheal language runner and compiler for JVM.

Intro
-----
Lamdheal is an easy functional language.
Focused on ease of learning and clear syntax, it is intended to be an agile JVM language and also a shell prompt scripting language. 
The project first goal is to finish a production quality runner (compiling under the hood);
the second one is to generate bytecode class files or a jar file ready to execute; and
the third goal is to compile indirectly to machine code via C code (without Java interaction support, of course).

For developpers:
It is highly dependent on Scala Parser Combinators and can be forked to easily suit your own language parsing needs.

License
-------
Lamdheal is under GPL, see COPYING for details.
Third party libraries are under different conditions,
please see SCALA-license.txt, YETI-license and JANINO-license for details.

Distribution of your applications written/compiled in lamdheal
---------------------------------------------------------
You can do whatever you want with your own applications written in lamdheal. Please note that your applications will depend on Yeti runtime.
Additionally, applications with embedded Java will depend on Janino library.
Please make the proper reference to the licenses.

