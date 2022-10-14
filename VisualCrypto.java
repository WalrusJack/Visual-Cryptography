
/*
 *	Author: Bruno Francisco
 *	
 *	Implementation of a k out of k visual cryptography scheme.
 *
*/

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import javax.imageio.ImageIO;

public class VisualCrypto {
	
	// Returns the powerset of 'set' (in the most ineffecient way possible)
	public static Set<Set<Integer>> powerSet(Set<Set<Integer>> pwrSet, Set<Integer> set) {
		if(set.isEmpty()) {
			pwrSet.add(new HashSet<Integer>());
			return pwrSet;
		}
		else {
			for(Integer e : set) {
				Set<Integer> nextSet = new HashSet<Integer>(set);
				nextSet.remove(e);
				Set<Set<Integer>> subSet = powerSet(pwrSet, nextSet);
				pwrSet.addAll(subSet);
				Set<Integer> eSet = new HashSet<Integer>();
				eSet.add(e);
				pwrSet.add(eSet);
				pwrSet.add(nextSet);
				pwrSet.add(set);
			}
			return pwrSet;
		}
	}
	
	// Takes an image as a command line argument
	public static void main(String[] args) {
		
		if(args.length == 1) {
			BufferedImage img = null;
			try {
				// Load the image provided by the user and convert to a black and white image
				img = ImageIO.read(new File(args[0]));
				int imgWidth = img.getWidth();
				int imgHeight = img.getHeight();
				BufferedImage bwImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_BINARY);
				Graphics2D g2d = bwImg.createGraphics();
				g2d.drawImage(img, 0, 0, null);
				g2d.dispose();
				
				// Ask user for the number of shares needed to reveal the image(k) and the total number of shares(n)
				Scanner scanner = new Scanner(System.in);
				System.out.print("Total shares(n): ");
				int n = scanner.nextInt();
				System.out.print("Needed shares(k): ");
				int k = scanner.nextInt();
				scanner.close();
				
				// Create an array of the first 'k' positive integers and find its powerset
				Set<Set<Integer>> pwrSet = new HashSet<Set<Integer>>();
				Set<Integer> set = new HashSet<Integer>();
				for(int i = 0; i < k; i++)
					set.add(i);
				pwrSet = VisualCrypto.powerSet(pwrSet, set);
				Set<Integer>[] pwrArray = pwrSet.toArray(new HashSet[0]);
				Integer[] setArray = set.toArray(new Integer[0]);
				
				// Seperate the powerset into two, one with the even sized sets and one with the odd sized sets
				final int COL_NUM = (int) Math.pow(2, k - 1);
				Set[] evenArray = new HashSet[COL_NUM];
				Set[] oddArray = new HashSet[COL_NUM];
				
				for(int i = 0, j = 0; i < pwrArray.length; i++) {
					if(pwrArray[i].size() % 2 == 0) {
						evenArray[j] = pwrArray[i];
						j++;
					}
				}
				
				for(int i = 0, j = 0; i < pwrArray.length; i++) {
					if(pwrArray[i].size() % 2 == 1) {
						oddArray[j] = pwrArray[i];
						j++;
					}
				}
				
				// Construct matrices representing the subpixels in the rows and the shares in the columns
				int[][] s0 = new int[k][COL_NUM];
				int[][] s1 = new int[k][COL_NUM];
				
				for(int i = 0; i < k; i++) {
					for(int j = 0; j < COL_NUM; j++) {
						if(evenArray[j].contains(setArray[i]))
							s0[i][j] = 1;
						else
							s0[i][j] = 0;
					}
				}
				
				for(int i = 0; i < k; i++) {
					for(int j = 0; j < COL_NUM; j++) {
						if(oddArray[j].contains(setArray[i]))
							s1[i][j] = 1;
						else
							s1[i][j] = 0;
					}
				}
				
				// Create the number of shares specified by the user
				final int SUB_WIDTH = (int) Math.sqrt(COL_NUM);
				BufferedImage[] shares = new BufferedImage[n];
				for(int i = 0; i < shares.length; i++) {
					shares[i] = new BufferedImage(imgWidth * SUB_WIDTH, imgHeight * SUB_WIDTH, BufferedImage.TYPE_BYTE_BINARY);
				}
				
				// Generate the image pixels using a random permutation of the matrix columns
				Random gen = new Random();
				for(int x = 0; x < imgWidth; x++) {
					for(int y = 0; y < imgHeight; y++) {
						
						ArrayList<Integer> tempCol = new ArrayList<Integer>();
						for(int i = 0; i < COL_NUM; i++)
							tempCol.add(i);
						
						int[] perm = new int[COL_NUM];
						for(int i = 0; i < perm.length; i++) {
							int rand = gen.nextInt(COL_NUM - i);
							perm[i] = tempCol.get(rand);
							tempCol.remove(rand);
						}
						
						if(bwImg.getRGB(x, y) == 0xFFFFFFFF) {
							for(int sx = 0; sx < SUB_WIDTH; sx++) {
								for(int sy = 0; sy < SUB_WIDTH; sy++) {	
									for(int i = 0; i < shares.length; i++) {
										shares[i].setRGB(x * SUB_WIDTH + sx, y * SUB_WIDTH + sy, s0[i][perm[i]] * 0xFFFFFFFF);
									}
								}
							}
						}
						else {
							for(int sx = 0; sx < SUB_WIDTH; sx++) {
								for(int sy = 0; sy < SUB_WIDTH; sy++) {
									for(int i = 0; i < shares.length; i++) {
										shares[i].setRGB(x * SUB_WIDTH + sx, y * SUB_WIDTH + sy, s1[i][perm[i]] * 0xFFFFFFFF);
									}
								}
							}
						}
					}
				}
				
				// Write the transparencies created into a file
				for(int i = 0; i < shares.length; i++) {
					ImageIO.write(shares[i], "png", new File("share" + i + ".png"));
				}
			}
			catch(IOException e) {
				System.out.println("Could not read the file.");
			}
		}
		else {
			System.err.println("Please specify an image to share.");
		}
		
	}
	
}
