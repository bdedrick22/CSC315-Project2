import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.io.Source
import java.io.PrintWriter
import java.io.File

/**
 * This class contains all necessary functions for encrypting and decrypting using a Playfair cipher.
 * I worked closely with Nathan and Jakob, we helped one another with just about every aspect of the project.
 * I also found code on alvinalexander.com for reading and writing to file, from stackoverflow code for working
 *    backwards on a map and from geeksforgeeks help for 2D arrays.
 * I had a personal goal to not use any for or while loops in the program.
 * I succeeded in that mostly by using the foreach function that you can call on lists and one recursive function.
 * @param phrase - The phrase on which we build our cipher. Default case will result in the cipher A-Z (no J).
 */
class Playfair (phrase: String = ""){
  /**
   * This case class is used for storing the location of each letter of the cipher.
   * A case class is similar to a Kotlin "data class".
   * I chose to use a case class over a Pair because it's easier to understand what the elements are.
   * @param row Row of point
   * @param column Column of point
   */
  private case class Point(row: Int, column: Int)

  // init
  // cleanPhrase is the phrase given, but cleaned using the cipherPhrase function.
  private val cleanPhrase = cipherPhrase(phrase)
  // cipher is a map the connects each character to a point.
  private val cipher =  collection.mutable.Map[Char, Point]()
  // This foreach function call fills the cipher with a point mapped to each letter.
  // The row is the index / 5 where as the column is the index % 5.
  cleanPhrase foreach { letter => cipher += (letter -> Point(cleanPhrase.indexOf(letter) / 5, cleanPhrase.indexOf(letter) % 5 ))}
  // end init

  /**
   * This is the public encode function.
   * It takes some message as a string, and returns that string in encoded form.
   * @param message Input Message
   * @return Encoded string
   */
  def encode(message: String) : String = decipher(createPairsRec(message), true).mkString

  /**
   * This is the public decode function.
   * It takes some message as a string, and returns that string in decoded form.
   * @param message Input message
   * @return Decoded message
   */
  def decode(message: String): String = decipher(createPairsRec(message), false).mkString

  /**
   * This function takes some message and returns a cleaned version separated into pairs.
   * It is a recursive function, each time it adds a pair to the list then calls the function on the remaining string.
   * @param remaining The remaining part of the message
   * @return A ListBuffer of type String of pairs.
   */
  private def createPairsRec(remaining: String): ListBuffer[String] = {
    // This cleans the message, making all letters uppercase, removing any non-letter characters, and replacing
    //    all instances of 'J' with 'I'.
    val msg = remaining.toUpperCase.filter(_.isLetter).replace('J', 'I')
    // List that stores all pairs.
    val lst: ListBuffer[String] = new ListBuffer[String]
    // First base case: message length == 1.
    //  If the message has only one element left, it that element + 'X' to the list and returns the list.
    // Second base case: message length == 2.
    //  In most cases (the else) it will add the pair to the list and return the list.
    //  In the case that the last two elements are equal to one another we have to add two pairs of that element + 'X'.
    // Third case is only if the user entered no message.
    if(msg.length == 1)
      return lst += (msg + "X")
    else if ( msg.length == 2) {
      return if (msg.charAt(0) == msg.charAt(1) && msg.charAt(0) != 'X') lst += (msg.charAt(0) + "X", msg.charAt(1) + "X")
      else lst += msg
    }
    else if (msg.length == 0)
      return lst
    // In the case that the first two elements of the list are equal, it add the first element + 'X' to the list,
    //    then returns that list concatenated with the list generated by the remainder of the string.
    if(msg.charAt(0) == msg.charAt(1))
      return (lst += (msg.charAt(0) + "X")) ++ createPairsRec(msg.substring(1))
    // Returns the list + the first pair and the list generated by the rest of th string.
    (lst += msg.substring(0, 2)) ++ createPairsRec(msg.substring(2))
  }

  /**
   * This function takes some message, either encoded or not, and performs transformations using the cipher.
   * @param message List of pairs to be encoded or decoded.
   * @param encode True for encode, False for decode.
   * @return A ListBuffer of type string of all the pairs with the necessary transformation.
   */
  private def decipher( message: ListBuffer[String], encode: Boolean): ListBuffer[String] = {
    // These three variables are the only things that change whether we are encoding or decoding.
    // If we are encoding, we shift right and down in rows and columns, so the wrapper needs to be from
    //    0 to 4 and the increment should be positive.
    // If we are decoding, we shift left and up in rows and columns, so the wrapper needs to be from 4
    //    to 0 and the increment should be negative (decrement).
    val startWrap = if(encode) 4 else 0
    val endWrap = if(encode) 0 else 4
    val increment = if (encode) 1 else -1
    // This list stores the new deciphered pairs.
    val myList = new ListBuffer[String]
    // For each element in the pairs, we have to check their point's relationships.
    // If the rows are equal to one another, we get the elements of the position one to the right.
    // If the columns are equal to one another, we get the elements of the position one down.
    // If neither are true, we get the element of the position that is the same row as itself,
    //    of the other.
    message foreach { pair => val p0 = cipher(pair.charAt(0)); val p1 = cipher(pair.charAt(1)); myList += (
      if(p0.row == p1.row)
        cipher.find(_._2 == Point(p0.row, if (p0.column == startWrap) endWrap else p0.column + increment)).get._1.toString +
        cipher.find(_._2 == Point(p1.row, if (p1.column == startWrap) endWrap else p1.column + increment)).get._1.toString
      else if (p0.column == p1.column)
        cipher.find(_._2 == Point(if (p0.row == startWrap) endWrap else p0.row + increment, p0.column)).get._1.toString +
        cipher.find(_._2 == Point(if (p1.row == startWrap) endWrap else p1.row + increment, p1.column)).get._1.toString
      else
        cipher.find(_._2 == Point(p0.row, p1.column)).get._1.toString +
        cipher.find(_._2 == Point(p1.row, p0.column)).get._1.toString ) }
    myList
  }

  /**
   * This function takes the string given by the user and "cleans" it.
   * Cleaning in this case means that everything is put in uppercase, 'J's are replaced with 'I's and any non-letter
   *    characters are thrown out.
   * It also adds all letters of the alphabet (not J) so that if the phrase given is empty or missing some letter, it
   *    will be present in the cipher.
   * The last thing it does is convert the String into a list and then removes all redundant letters.
   * @param str Phrase given to be cleaned.
   * @return List of 25 unique characters for the cipher.
   */
  private def cipherPhrase(str:String): List[Char] =
    (str.toUpperCase.replace('J', 'I').filter(_.isLetter) + "ABCDEFGHIKLMNOPQRSTUVWXYZ").toList.distinct

  /**
   * This function will return the cipher in string format.
   * @return String of the cipher.
   */
  def getCipher(): String ={
    // I needed to create a new list because my map isn't a good format to try and read.
    // So here I created a 2D array.
    val lst = Array.ofDim[Char](5,5)
    // For each element in the cipher I add the value at each point to the corresponding part of the 2D array.
    cipher foreach {kv => lst(kv._2.row)(kv._2.column) = kv._1}
    // This maps each row to a string separated by a space then joins each row with a new line.
    lst.map{ row => row.mkString(sep = " ") }.mkString(sep = "\n" )
  }
}

/**
 * Object for testing the Playfair cipher.
 */
object Program2 extends App {

  // Phrase to generate the cipher.
  val phrase = "How vexingly quick daft zebras jump!"
  // Creates new instance of the Playfair cipher using the phrase.
  val playfair = new Playfair(phrase)

  println(playfair.getCipher())

  // The message to be encoded. It is simply a string, so if you don't wish for a file, you don't have to.
  val message = Source.fromFile("C:\\Users\\bramd\\Documents\\Playfair_Test.txt").getLines.mkString

  println(f"Message: $message")

  // Gets the encoded message.
  val encodedMessage = playfair.encode(message)

  // Sets up a writer for some file.
  val writer = new PrintWriter(new File("C:\\Users\\bramd\\Documents\\Playfair_Test_Output.txt"))
  // Writes the message to that file.
  writer.write(encodedMessage)
  writer.close()

  println(f"Encoded Message: $encodedMessage")

  // Gets decoded message from out encoded message.
  val decodedMessage = playfair.decode(encodedMessage)

  println(f"Decoded Message: $decodedMessage")
}