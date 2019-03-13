<img src="https://github.com/Zmwang622/Milk-Lang-and-Interpreter/blob/master/Milk.PNG" height=75% width = 75%>
**Testimonies:**
Hear about Milk from some of my good friends!

"Get off your horse and drink **Milk**." - [John Wayne](https://www.youtube.com/watch?v=IG455EGnCyk)

"Beverage wise, I stick to **Milk**." - [Derrick Rose](https://www.brainyquote.com/quotes/derrick_rose_541442?src=t_milk) (*Take that Java*) 

"I have a dream to provide every Chinese, especially children, sufficient **Milk** each day."  - [Wen Jiabao](https://www.walmart.com/ip/Wen-Jiabao-I-have-a-dream-to-provide-every-Chinese-especially-children-sufficient-milk-each-day-Famous-Quotes-Laminated-POSTER-PRINT-24X20/352561585)

"I love **Milk** so much!" - [Natalie Portman](https://www.brainyquote.com/quotes/natalie_portman_414141)

"Ming is the worst teacher ever" - Hasala Rannulu (so much for being best friends)
## What is it
I always wanted to make my own programming language. Senior year of HS, I first started to learn how to program and for our final project I jokingly said that my group and I should make our own language. Though we chose to make a game that didn't actually work, the idea stuck in my head. 

Fast forward to my first year of college. After a successful first semester, I was set on working on my first *real* personal project (because that's supposedly what gets jobs.) With the help of [this handy textbook](http://www.craftinginterpreters.com/) and the fact that I spent 30 hours on the road during my winter break, I was able to bust out this nifty Java interpreter in around a month. Definitely piggybacked off that book **alot** but still learned a lot. The next semester a lot of what I read in the book came up in my classes and I felt pretty cool. 

Also: I tried to add javadoc via Eclipse (because it's easier) and messed up the Java path thingies on my computer. Hopefully it still works via GitHub. 

## How it Works
Milk is a dynamically-typed, object-oriented, imperative, scripting language. It's not that robust but I love it. Also it's easy to understand for programming beginners and I taught two of my friends programming via this. 

### Data Types
Pretty basic stuff. 
* Booleans: true or false (crazy)
* Numbers: Only one type- double-precision floating point. 1234 and 12.34 all fit under the same variable.
* Strings: Enclosed in "double quotes". ""; "crazy stuff!";
* Nil: Had to distinguish the null value with Java's null, so used Nil.

### Expressions
A lot of stuff going on here. 

*Arithmetic*
Just like normal.
```
add + this;
subtract - me; 
multiply * me;
divide / this;
-negateThis
```
*Comparisons*
```
less < than
lessThan =< orEqual
greater > than
greaterThan >= orEqual

1==2;       //false
"cat"!="dog";  //true
//You can compare different types!
314 == "pi"; //false
123 == "123"; //false
```
*Logic*
```
!true;  //false
!false; //true
true and false; //false
true and true;  //true
false or false; //false
true or true;   //true

// Use () to change precedence
var avg =  (min + max) / 2;
```

### Statements
Statements produce an *effect*. The print statement is an example. You can pack a series of statements

```
{
  print "One statement.";
  print "Two statements.";
}
```

### Variables
Like other languages, use = to initialize variables. Omitting the initializer defaults to nil. Because Milk is *dynamically-typed* there is only one key word for defining variables, `var`.

```
var hello = "World";
var stillAVar;
```

Access and assign variables using the name

```
var breakfast = "bagels";
print breakfast; // "bagels".
breakfast = "beignets";
print breakfast; // "beignets".
```

### Control Flow
There are if, for, and while statements.

```
// If/Else statements
if(condition){
  print "correct";
}
else{
  print "wrong";
}

// While Loop
var a = 0;
while(a<10)
{
 print "Ming is so cool";
 a = a + 1;
}

// For Loop
for(var a = 1; a < 10; a = a +5)
{
    print a;
}
```

### Functions
Really let the power get to my head at this point. 

Functions can be run with or without arguments and look just like Java's functions.
```
doThing()
build(brick, clay)
```

Use the keyword `ming` to declare functions...Clever right? LOL.
```
// No I'm not joking.
ming printSum(a,b)
{
  print a + b;
}
```

Functions are *first class* in Milk, meaning they work just real values. Imagine function pointers in C.
```
ming addPair(a, b) {
  return a + b;
}

ming identity(a) {
  return a;
}

print identity(addPair)(1, 2); // Prints "3".
```

Functions can be declared within functions. It's a pretty cool feature. Shout out to Bob Nystrom (the author of the book).
```
ming returnFunction() {
  var outside = "outside";

  ming inner() {
    print outside;
  }

  return inner;
}

var woah = returnFunction();
fn();
```

### Classes
Milk is OOP. Here's what one would look like.
```
class Breakfast {
  cook() {
    print "Eggs a-fryin'!";
  }

  serve(who) {
    print "Enjoy your breakfast, " + who + ".";
  }
}

// Store it in variables.
var someVariable = Breakfast;

// Pass it to functions.
someFunction(Breakfast);

// Print it 
print someVariable; // "Breakfast instance"
```

*Instances and Initialization*
Use init() to create a constructor-sorta-thing.
```
class Breakfast {
  init(meat, bread) {
    this.meat = meat;
    this.bread = bread;
  }

  // ...
}

var baconAndToast = Breakfast("bacon", "toast");
baconAndToast.serve("Dear Reader");
```
*Inheritance*
Use `className<superClass` to get inheritance working.
```
class Brunch < Breakfast {
  drink() {
    print "How about a Blood Mary?";
  }
}
```
In the above example Brunch is the subclass and thus is able to use any of Breakfast's methods. But when Brunch calls drink() the output is different than what Breakfast would output.


## How to Use 
0. Download the repos from Github.
0. On command prompt/terminal/whatever, cd into where Milk is located.
1. javac Milk.java
2. java Milk [arg]

If there's no argument you enter a Milk environment, just like Python's virtual environment!

I hope to make an online IDE version of Milk in the future, would be pretty cool.

```
//This is how Milk works!
print"Thanks for making it this far! <3";
```

