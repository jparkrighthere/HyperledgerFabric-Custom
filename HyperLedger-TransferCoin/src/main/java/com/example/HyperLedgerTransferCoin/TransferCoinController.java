package com.example.HyperLedgerTransferCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransferCoinController {

    public static class Account {
        public AccountType accountType;
        public String owner;
        public Double balance;
    }

    public static class Transaction {
        public String sender;
        public String receiver;
        public Double money;
    }

    @Autowired
    private TransferCoinService transferCoinService;

    @RequestMapping("/")
    public String run() throws Exception {
        return "main.html";
    }

    @ResponseBody
    @PostMapping("account/create")
    public void createAccount(@RequestBody Account account) throws Exception {
        transferCoinService.createAccount(String.valueOf(account.accountType), account.owner, account.balance);
    }

    @ResponseBody
    @GetMapping("account/list")
    public String listAccount() throws Exception {
        return transferCoinService.listAccounts();
    }

    @ResponseBody
    @PostMapping("account/transaction")
    public void transferAsset(@RequestBody Transaction t) throws Exception {
        System.out.println(t.money + t.sender + t.receiver);
        transferCoinService.transferAsset(t.sender,t.receiver, t.money);
    }

}
