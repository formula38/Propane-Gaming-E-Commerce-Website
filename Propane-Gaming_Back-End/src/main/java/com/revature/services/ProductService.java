package com.revature.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.revature.dtos.ProductInfo;
import com.revature.models.Product;
import com.revature.models.StopWords;
import com.revature.repositories.ProductRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	final String REGEX_PUNCT = "\\p{Punct}";

	public List<Product> findByDescription(String description) {
		String sanitizedInput = stripPunctuationAndStopWords(description);

		String searchQuery = Arrays.stream(sanitizedInput.split(" "))
				.collect(Collectors.joining("|", "(", ")"));

		return productRepository.findByDescriptionContainingIgnoreCase(searchQuery);
	}

	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public Optional<Product> findById(int id) {
		return productRepository.findById(id);
	}

	public Product save(Product product) {
		return productRepository.save(product);
	}

	public List<Product> saveAll(List<Product> productList, List<ProductInfo> metadata) {
		return productRepository.saveAll(productList);
	}

	public void delete(int id) {
		productRepository.deleteById(id);
	}

	private Set<Product> editDistanceSearch(List<Product> allProd, Set<Product> filteredProds, String input) {
		for (Product p : allProd) {
			String pName = p.getName();
			int[][] dist = new int[pName.length() + 1][input.length() + 1];

			for (int i = 0; i < pName.length() + 1; i++) {
				for (int j = 0; j < input.length() + 1; j++) {
					if (i * j == 0) {
						dist[i][j] = (i == 0 ? j : i);
					} else {
						dist[i][j] = Math.min(
								Math.min(dist[i - 1][j - 1] + ((pName.charAt(i - 1) == input.charAt(j - 1)) ? 0 : 1),
										dist[i - 1][j] + 1),
								dist[i][j - 1] + 1);
					}
				}
			}
			if (input.length() != 0 && dist[pName.length()][input.length()] <= pName.length() / 2) {
				filteredProds.add(p);
			}
		}
		return filteredProds;
	}

	public Set<Product> findBySimilarNameDescription(String input) {

		String sanitizedInput = stripPunctuationAndStopWords(input);

		String searchQuery = Arrays.stream(sanitizedInput.split(" "))
				.collect(Collectors.joining("|", "(", ")"));

		List<Product> allProd = new ArrayList<>(productRepository.findAll());

		Set<Product> filteredProds = new HashSet<>();

		filteredProds.addAll(productRepository.findBySimilarName(searchQuery));
		filteredProds.addAll(productRepository.findByDescriptionContainingIgnoreCase(searchQuery));

		return editDistanceSearch(allProd, filteredProds, sanitizedInput);
	}

	public List<Product> searchByPriceRange(double startPrice, double endPrice) {
		return productRepository.priceRangeSearch(startPrice, endPrice);
	}

	public List<Product> searchByTag(String tagName) {
		return productRepository.tagSearch(tagName);
	}

	public Set<Product> superSearch(double startPrice, double endPrice, String tagName, String input) {

		String sanitizedInput = stripPunctuationAndStopWords(input);

		String searchQuery = Arrays.stream(sanitizedInput.split(" "))
				.collect(Collectors.joining("|", "(", ")"));

		Set<Product> filteredProds = new HashSet<>();

		List<Product> allProd = new ArrayList<>(productRepository.findAll());

		if (tagName.equals("NULL")) {
			filteredProds.addAll(productRepository.superSearchWithoutTag(startPrice, endPrice, searchQuery));
		} else {
			filteredProds.addAll(productRepository.superSearchWithTag(startPrice, endPrice, tagName, searchQuery));
		}

		return editDistanceSearch(allProd, filteredProds, sanitizedInput);
	}

	private String stripPunctuationAndStopWords(String input) {
		return Arrays.stream(input.split(" ")).map(String::trim)
				.map(word -> word = word.replaceAll(REGEX_PUNCT, ""))
				.filter(word -> !word.isEmpty() && word.length() > 1 && !StopWords.contains(word))
				.collect(Collectors.joining(" "));
	}
}