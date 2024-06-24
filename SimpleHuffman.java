

import java.io.*;
import java.util.*;

/**
 * @author Manzi Fabrice Niyigaba
 * Purpose: To compress and decompress a file using huffman's approach
 * Date: May 2, 2024
 */


public class SimpleHuffman implements Huffman {
    /**
     * Read file provided in pathName and count how many times each character appears
     * @param pathName - path to a file to read
     * @return - Map with a character as a key and the number of times the character appears in the file as value
     * @throws IOException
     */
    @Override
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {
        BufferedReader input = null;
        Map<Character, Long> myMap= new HashMap<>();
        try {
            input= new BufferedReader(new FileReader(pathName));
            char character;
            int x;
            //checks if character is in the map and add if not and increase frequency by one if it is
            while ((x= input.read()) != -1){
                character = (char) x;
                if (!myMap.containsKey(character)){
                    myMap.put(character, (long) 1);
                } else {
                    myMap.put(character, myMap.get(character)+ 1);
                }

            }
        } catch (IOException  e){
            System.out.println("Invalid File Path");
        } finally {
           //ensures that the file is closed
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println("Failed to close the file ");
                }
            }
        }

        return myMap;
    }

    /**
     * class that implements comparator and override compare
     */

    private static class FrequencyComparator implements Comparator<BinaryTree<CodeTreeElement>>{

        /**
         * method that compares the two tree frequencies
         * @param tree1 the first object to be compared.
         * @param tree2 the second object to be compared.
         * @return
         */
        @Override
        public int compare(BinaryTree<CodeTreeElement> tree1, BinaryTree<CodeTreeElement> tree2) {
            //return one if t1 is greater, -1 if it is lesser and zero if they are equal
            Long freq1 = tree1.getData().getFrequency();
            Long freq2 = tree2.getData().getFrequency();
            if (freq1 > freq2) return 1;
            else if (freq1 < freq2) return -1;
            else {
                return 0;
            }
        }

    }


    /**
     * Construct a code tree from a map of frequency counts. Note: this code should handle the special
     * cases of empty files or files with a single character.
     *
     * @param frequencies a map of Characters with their frequency counts from countFrequencies
     * @return the code tree.
     */


    @Override
    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        //return if map is empty
        if (frequencies.isEmpty()){
            return null;
        }

        //passes frequency compare overide comparator to priority queue
        Comparator<BinaryTree<CodeTreeElement>> frequencyCompare = new FrequencyComparator();
        PriorityQueue<BinaryTree<CodeTreeElement>> minQueue = new PriorityQueue<>(frequencyCompare);


        for (char key: frequencies.keySet()){
            //create a combined node of key and its frequency
            CodeTreeElement data = new CodeTreeElement(frequencies.get(key), key);
            //create a new tree with the combined node as data
            BinaryTree<CodeTreeElement> root = new BinaryTree<>(data);
            minQueue.add(root);

        }

        if (minQueue.size() == 1) {
            // Create a dummy parent node for the single node to maintain the structure of Huffman tree.
            BinaryTree<CodeTreeElement> singleNode = minQueue.remove();
            CodeTreeElement dummyRoot = new CodeTreeElement(singleNode.getData().getFrequency(), null);
            return new BinaryTree<>(dummyRoot, singleNode, null);
        }

        //create a new tree with combined frequency of T1 and T2(two smallest frequency) as root and left as T1 and right as T2
        while (minQueue.size() > 1){
            BinaryTree<CodeTreeElement> T1 = minQueue.remove();
            BinaryTree<CodeTreeElement> T2 = minQueue.remove();
            CodeTreeElement r = new CodeTreeElement(T1.getData().getFrequency() + T2.getData().getFrequency(),null );
            BinaryTree<CodeTreeElement> tree = new BinaryTree<>(r,T1,T2);
            minQueue.add(tree);

        }

        return minQueue.remove();
    }




    /**
     * Computes the code for all characters in the tree and enters them
     * into a map where the key is a character and the value is the code of 1's and 0's representing
     * that character.
     *
     * @param codeTree the tree for encoding characters produced by makeCodeTree
     * @return the map from characters to codes
     */

    @Override
    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree) {
        Map<Character, String> codeMap = new HashMap<>();

        if (codeTree != null && codeTree.getData() != null) {
            computeCodesHelper(codeTree, "", codeMap); // Start with an empty string for the root
        }
        return codeMap;
    }

    private void computeCodesHelper(BinaryTree<CodeTreeElement> codetree, String currentCode, Map<Character, String> codeMap) {
        if (codetree == null) {
            return;
        }

        // Check if it's a leaf node
        if (codetree.isLeaf()) {
            codeMap.put(codetree.getData().getChar(), currentCode);
        } else {
            if (codetree.hasLeft()){
                //add 0 if you go left
                computeCodesHelper(codetree.getLeft(), currentCode + "0", codeMap);
            }
            //add 1 if you go right
            if (codetree.hasRight()){
                computeCodesHelper(codetree.getRight(), currentCode + "1", codeMap);
            }

        }
    }




    /**
     * Compress the file pathName and store compressed representation in compressedPathName.
     * @param codeMap - Map of characters to codes produced by computeCodes
     * @param pathName - File to compress
     * @param compressedPathName - Store the compressed data in this file
     * @throws IOException
     */
    @Override

    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {
        // Open the input file for reading characters
        BufferedReader input= null;

        // Open the output file for writing compressed data
        BufferedBitWriter bitOutput= null;
        try{
            // Open the input file for reading characters
            input = new BufferedReader(new FileReader(pathName));

            // Open the output file for writing compressed data
            bitOutput = new BufferedBitWriter(compressedPathName);
            // Read characters from the input file and write their corresponding codes to the output file
            int x;
            while ((x = input.read()) != -1) {
                char character = (char) x;
                String code = codeMap.get(character);
                // Write each bit of the code to the output file
                for (int i = 0; i < code.length(); i++) {
                    char bit = code.charAt(i);
                    if (bit == '0') {
                        bitOutput.writeBit(false);
                    } else {
                        bitOutput.writeBit(true);
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Invalid File");
        } finally {
            // Close the input and output files
            if (input != null) {
                input.close();
            }
            if (bitOutput != null) {
                bitOutput.close();
            }
        }


    }

    /**
     * Decompress file compressedPathName and store plain text in decompressedPathName.
     * @param compressedPathName - file created by compressFile
     * @param decompressedPathName - store the decompressed text in this file, contents should match the original file before compressFile
     * @param codeTree - Tree mapping compressed data to characters
     * @throws IOException
     */
    @Override
    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {
        // initialize the input file for reading compressed data
        BufferedBitReader bitInput = null;

        // initialize output file for writing decompressed characters
        BufferedWriter output = null;
        try{
            // Open the input file for reading compressed data
            bitInput = new BufferedBitReader(compressedPathName);

            // Open the output file for writing decompressed characters
            output = new BufferedWriter(new FileWriter(decompressedPathName));
            // Start at the root of the code tree
            BinaryTree<CodeTreeElement> currentNode = codeTree;

            // Read bits from the input file and traverse the code tree
            while (bitInput.hasNext()) {
                boolean bit = bitInput.readBit();
                // If bit is false, go left in the code tree
                if (!bit) {
                    currentNode = currentNode.getLeft();
                } else { // If bit is true, go right in the code tree
                    currentNode = currentNode.getRight();
                }
                // If currentNode is a leaf, write its character to the output file and return to the root
                if (currentNode.isLeaf()) {
                    char character = currentNode.getData().getChar();
                    output.write(character);
                    currentNode = codeTree; // Return to the root of the code tree
                }
            }
        } catch (Exception e){
            System.out.println("Invalid File");
        } finally {
            // Close the input and output files
            if (bitInput != null) {
                bitInput.close();
            }
            if (output != null) {
                output.close();
            }
        }

    }

    public static void main(String[] args) {
        // Test file paths
        String usConstitutionFilePath = "USConstitution.txt";
        String testFilePath = "test.txt";
        String warAndPeaceFilePath = "WarAndPeace.txt";

        // Create Huffman encoder
        SimpleHuffman huffman = new SimpleHuffman();

        // Count frequencies for US Constitution
        Map<Character, Long> usConstitutionFrequencies;
        try {
            usConstitutionFrequencies = huffman.countFrequencies(usConstitutionFilePath);
        } catch (IOException e) {
            System.err.println("Error counting frequencies for US Constitution: " + e.getMessage());
            return;
        }

        // Construct code tree for US Constitution
        BinaryTree<CodeTreeElement> usConstitutionCodeTree = huffman.makeCodeTree(usConstitutionFrequencies);

        // Compute codes for US Constitution
        Map<Character, String> usConstitutionCodeMap = huffman.computeCodes(usConstitutionCodeTree);

        // Compress US Constitution
        try {
            huffman.compressFile(usConstitutionCodeMap, usConstitutionFilePath, "usConstitutionCompressed.huff");
            System.out.println("US Constitution compressed successfully.");
        } catch (IOException e) {
            System.err.println("Error compressing US Constitution: " + e.getMessage());
        }

        // Count frequencies for test file
        Map<Character, Long> testFileFrequencies;
        try {
            testFileFrequencies = huffman.countFrequencies(testFilePath);
        } catch (IOException e) {
            System.err.println("Error counting frequencies for test file: " + e.getMessage());
            return;
        }

        // Construct code tree for test file
        BinaryTree<CodeTreeElement> testFileCodeTree = huffman.makeCodeTree(testFileFrequencies);

        // Compute codes for test file
        Map<Character, String> testFileCodeMap = huffman.computeCodes(testFileCodeTree);

        // Compress test file
        try {
            huffman.compressFile(testFileCodeMap, testFilePath, "testCompressed.huff");
            System.out.println("Test file compressed successfully.");
        } catch (IOException e) {
            System.err.println("Error compressing test file: " + e.getMessage());
        }

        // Decompress US Constitution
        try {
            huffman.decompressFile("usConstitutionCompressed.huff", "usConstitutionDecompressed.txt", usConstitutionCodeTree);
            System.out.println("US Constitution decompressed successfully.");
        } catch (IOException e) {
            System.err.println("Error decompressing US Constitution: " + e.getMessage());
        }

        // Decompress test file
        try {
            huffman.decompressFile("testCompressed.huff", "testDecompressed.txt", testFileCodeTree);
            System.out.println("Test file decompressed successfully.");
        } catch (IOException e) {
            System.err.println("Error decompressing test file: " + e.getMessage());
        }

        // Only compress WarAndPeace.txt
        // Count frequencies for WarAndPeace.txt
        Map<Character, Long> warAndPeaceFrequencies;
        try {
            warAndPeaceFrequencies = huffman.countFrequencies(warAndPeaceFilePath);
        } catch (IOException e) {
            System.err.println("Error counting frequencies for WarAndPeace.txt: " + e.getMessage());
            return;
        }

        // Construct code tree for WarAndPeace.txt
        BinaryTree<CodeTreeElement> warAndPeaceCodeTree = huffman.makeCodeTree(warAndPeaceFrequencies);

        // Compute codes for WarAndPeace.txt
        Map<Character, String> warAndPeaceCodeMap = huffman.computeCodes(warAndPeaceCodeTree);

        // Compress WarAndPeace.txt
        try {
            huffman.compressFile(warAndPeaceCodeMap, warAndPeaceFilePath, "WarAndPeaceCompressed.huff");
            System.out.println("WarAndPeace.txt compressed successfully.");
        } catch (IOException e) {
            System.err.println("Error compressing WarAndPeace.txt: " + e.getMessage());
        }
    }
    }








