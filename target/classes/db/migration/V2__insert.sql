INSERT INTO payments (id, amount, currency, method, user_id, is_fraud, status)
VALUES 
    ('pay_12345abcde', 150.75, 'EUR', 'credit_card', 'user_67890', false, 'completed'),
    ('pay_67890fghij', 89.99, 'USD', 'paypal', 'user_12345', false, 'pending'),
    ('pay_54321klmno', 250.00, 'GBP', 'bank_transfer', 'user_98765', true, 'failed'),
    ('pay_98765pqrst', 45.50, 'EUR', 'debit_card', 'user_11111', false, 'completed');