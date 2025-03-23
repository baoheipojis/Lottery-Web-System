@RestController
@RequestMapping("/api/prizes")
public class PrizeController {
    
    @Autowired
    private PrizeService prizeService;
    
    @GetMapping
    public List<Prize> getAllPrizes() {
        return prizeService.getAllPrizes();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prize addPrize(@RequestBody Prize prize) {
        return prizeService.savePrize(prize);
    }
}