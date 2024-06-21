-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 19, 2024 at 03:36 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventory_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `o_id` int(50) NOT NULL,
  `o_cname` varchar(50) NOT NULL,
  `o_name` varchar(50) NOT NULL,
  `o_price` varchar(50) NOT NULL,
  `o_stocks` varchar(50) NOT NULL,
  `o_status` varchar(50) NOT NULL,
  `o_method` varchar(50) NOT NULL,
  `o_quantity` int(50) NOT NULL,
  `o_address` varchar(50) NOT NULL,
  `total_profit` varchar(50) DEFAULT NULL,
  `o_approve` varchar(50) DEFAULT NULL,
  `o_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`o_id`, `o_cname`, `o_name`, `o_price`, `o_stocks`, `o_status`, `o_method`, `o_quantity`, `o_address`, `total_profit`, `o_approve`, `o_date`) VALUES
(23, 'potanginamo', 'asd', '10', '0', 'AVAILABLE', 'CASH ON DELIVERY', 5, 'yawa wako kahibalo', '50.0', 'Delivered', '2024-06-18');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `p_id` int(20) NOT NULL,
  `p_name` varchar(20) NOT NULL,
  `p_price` varchar(20) NOT NULL,
  `p_stocks` varchar(20) NOT NULL,
  `p_status` varchar(20) NOT NULL,
  `p_image` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`p_id`, `p_name`, `p_price`, `p_stocks`, `p_status`, `p_image`) VALUES
(23, 'asd', '10', '0', 'AVAILABLE', 'src/ProductsImage/circle-user.png');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(50) NOT NULL,
  `email` varchar(50) NOT NULL,
  `contact` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(250) NOT NULL,
  `type` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `Image` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `contact`, `username`, `password`, `type`, `status`, `Image`) VALUES
(21, 'atay@gmail.com', '11111111111', 'potanginamo', '9bsMjeFGxntEurv05lhMwA==', 'ADMIN', 'Active', 'src/ImageDB/yawa (1).png'),
(22, '123@gmail.com', '12312312312', '123213213231', '9bsMjeFGxntEurv05lhMwA==', 'ADMIN', 'ACTIVE', 'src/ImageDB/1.png');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`o_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`p_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `o_id` int(50) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `p_id` int(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(50) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
